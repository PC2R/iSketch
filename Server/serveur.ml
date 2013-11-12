(* Game parameters *)
let max_players = ref 4
(*let incr_counter = let c = ref 0 in (fun () -> incr c; !c);;*)
let players_connected = ref 0
let timeout = ref 30
let port = ref 2013
let players_points = ref ((Hashtbl.create !max_players) : (string, int) Hashtbl.t)
let verbose_mode = ref false

let m = Mutex.create ()
let c = Condition.create ()

(* Dictionary characteristics *)
let dictionary_filename = ref "dictionary"
let dictionary_words = ref (Array.make 0 "")
let dictionary_size = ref 0

(* Round parameters *)
let round = ref 0
let accept_players = ref true
let is_started = ref false
let word = ref ""
let word_is_found = ref false
let running_order = ref (Array.make 0 "")
let players_roles = ref ((Hashtbl.create !max_players) : (string, string) Hashtbl.t)

let add_player name = 
  if (!players_connected != !max_players) then
    begin
      Hashtbl.add !players_points name 0;
      Array.set !running_order !players_connected name;
      incr players_connected;
      if (!players_connected = !max_players) then
	Condition.signal c;
      Mutex.unlock m;
      true
    end
  else
    begin
      Mutex.unlock m;
      false
    end

let my_input_line file_descr =
  let s = " " and r = ref "" in
  while (Unix.read file_descr s 0 1 > 0) && s.[0] <> '\n' do
    r := !r ^ s
  done;
  !r;;

(* This function computes how many lines has the file given in parameter *)
let compute_size file =
  let n = ref 0 and in_channel = open_in file in
  let rec loop () = ignore (input_line in_channel); incr n; loop () in
  try loop () with End_of_file -> close_in in_channel; !n;;

exception Fin
exception Maximum_players_reached

class player sd (sa : Unix.sockaddr) =
object (self)
	 
  val s_descr = sd
  val s_addr = sa
  val mutable pseudo = ""
  val mutable number = 0
			     
  initializer
    if (!verbose_mode) then
      print_endline ("Someone has just open a connection on the server.");

  method start () =
    Thread.create (fun x ->
		   self#run x;
		   self#stop x
		  ) ();
    
  method stop () =
    if (pseudo != "") then
      begin
	decr players_connected;
	if (!verbose_mode) then
	  print_endline (pseudo ^ "has left the game.");
      end;
    Unix.close s_descr
	     
	       
  method run () =
    try
      while true do
	let command = my_input_line s_descr in
	let l = Str.split (Str.regexp "[/]") command in
	match List.nth l 0 with
	  "CONNECT" -> let name = String.sub command 8 (String.length command - 8) in
		       Mutex.lock m;
		       if (Hashtbl.mem !players_points name) then
			 begin
			   let result = "CONNECTION_REFUSED/" ^ name ^ "name_already_taken\n" in
			   ignore (Unix.write s_descr result 0 (String.length result));
			   Mutex.unlock m
			 end			   
		       else
			 if (add_player name) then
			   begin
			     let result = "CONNECTED/" ^ name ^ "\n" in
			     ignore (Unix.write s_descr result 0 (String.length result));
			     pseudo <- name;
			     print_endline (name ^ " has just joined the game. " ^ string_of_int (!max_players - !players_connected) ^ " player(s) missing before starting the game.");
			   end
			 else
			   begin
			     let result = "CONNECTION_REFUSED/" ^ name ^ "maximum_capacity_reached\n" in
			     ignore (Unix.write s_descr result 0 (String.length result));
			     raise Maximum_players_reached
			   end;
	| "EXIT" -> let name = String.sub command 5 (String.length command - 5) in
		    if (Hashtbl.mem !players_points name) then
		      begin
			let result = "EXITED/" ^ name ^ "\n" in
			ignore (Unix.write s_descr result 0 (String.length result));
			Hashtbl.remove !players_points name;
		      end
		    else
		      let result = name ^ "is not in the hashtable, it cannot be removed\n" in
		      ignore (Unix.write s_descr result 0 (String.length result));
	| "GUESS" -> let guessed_word = String.sub command 6 (String.length command - 6) in
		     let result = "GUESSED/" ^ guessed_word ^ pseudo ^ "\n" in
		     ignore (Unix.write s_descr result 0 (String.length result));
	| _ -> let result = command ^ " is unknown (try CONNECT/user/ or EXIT/user/)\n" in
	       ignore (Unix.write s_descr result 0 (String.length result));
      done;
    with
      Maximum_players_reached -> if (!verbose_mode) then
				   print_endline ("A player tried to join the game but maximum players capacity was reached.");
      | exn -> print_string (Printexc.to_string exn ^ "\n");			  
end;;
  
class server port n =
object(s)
	
  val port_num = port
  val nb_pending = n
  val s_descr = Unix.socket Unix.PF_INET Unix.SOCK_STREAM 0

  initializer
  let host = Unix.gethostbyname (Unix.gethostname()) in
      let h_addr = host.Unix.h_addr_list.(0) in
      let sock_addr = Unix.ADDR_INET (h_addr, port_num) in
      Unix.setsockopt s_descr Unix.SO_REUSEADDR true;
      Unix.bind s_descr sock_addr;
      Unix.listen s_descr nb_pending;
      if (!verbose_mode) then
	begin
	  print_endline ("Server successfuly started with parameters : " ^ string_of_int !max_players
			 ^ " maximum players and " ^ string_of_int !timeout ^ " seconds of timeout.");
	  print_endline ("Server is waiting for players connections on port " ^ string_of_int port ^ ".");
	end;
      running_order := Array.append !running_order (Array.make !max_players "");
      dictionary_size := compute_size !dictionary_filename;
      dictionary_words := Array.append !dictionary_words (Array.make !dictionary_size "");
      begin
	let in_channel = open_in !dictionary_filename and i = ref 0 in
	let rec loop () =
	  Array.set !dictionary_words !i (input_line in_channel);
	  incr i;
	  loop ()
	in
	try
	  loop ()
	with End_of_file -> close_in in_channel
      end;
      
  method start () =
    ignore (s#start_game ());
    while (true) do
      let (service_sock, client_sock_addr) =
	Unix.accept s_descr in
      s#treat service_sock client_sock_addr;
    done;
    
  method treat service_sock client_sock_addr =
    ignore ((new player service_sock client_sock_addr)#start())    

  method start_game () =
    Thread.create (fun x -> s#p x) ();

  method p () =
    Condition.wait c m;
    while (!round < !max_players) do
      if (!verbose_mode) then
	print_endline ("Round " ^ string_of_int (!round + 1) ^ "/" ^ string_of_int (!max_players) ^" !");
      if (!verbose_mode) then
	print_endline (Array.get !running_order !round ^ " is the drawer");
      word := !dictionary_words.((Random.int (!dictionary_size)));
      if (!verbose_mode) then
	print_endline ("The drawer needs to draw the word \"" ^ !word ^ "\"");
      incr round;
    done
      
end;;
  
let main () =
  begin
    let speclist =
      [("-max", Arg.Set_int max_players, " Sets maximum number of players (default : 4)");
       ("-timeout", Arg.Set_int timeout, " Sets the length of the timeout (in seconds) when the word is found (default : 30)");
       ("-port", Arg.Set_int port, " Sets the listening port for the server (default : 2013)");
       ("-dico", Arg.Set_string dictionary_filename, " Dictionary");
       ("-v", Arg.Set verbose_mode, " Enables verbose mode");
      ]
    in let usage_msg = "Options availables for iSketch server :"
       in Arg.parse speclist print_endline usage_msg;
  end;
  Random.self_init();
  (new server !port 2)#start();;
  
  main ();;
