(* Game parameters *)
let max_players = ref 4
(*let incr_counter = let c = ref 0 in (fun () -> incr c; !c);;*)
let players_connected = ref 0
let timeout = ref 30
let port = ref 2013
let players_points = ref ((Hashtbl.create !max_players) : (string, int) Hashtbl.t)
let verbose_mode = ref false

(* Dictionary characteristics *)
let dictionary_filename = ref ""
let dictionary_words = ref (Array.make 0 "")
let dictionary_size = ref 0

(* Round parameters *)
let accept_players = ref true
let is_started = ref false
let drawer = ref 0
let word = ref ""

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

exception Fin;;

class connection sd (sa : Unix.sockaddr) waiting_player =
object (self)
	 
  val s_descr = sd
  val s_addr = sa
  val mutable pseudo = ""
  val mutable number = 0
  val mutable is_waiting = false
			     
  initializer
    is_waiting <- waiting_player;
    if not is_waiting then
      begin
	if (!verbose_mode) then
	  begin
	    incr players_connected;
	    number <- !players_connected;
	    print_endline ("Player n°" ^ string_of_int number ^ " has joined the game.");
	  end
      end
    else
      if (!verbose_mode) then
	print_endline ("A player tried to join the game but maximum players capacity was reached.")
		      
  method start () =
    Thread.create (fun x ->
		   if not is_waiting then
		     begin
		       self#run x;
		       self#stop x
		     end
		   else
		     begin
		       self#stop x
		     end
		  ) ();
    
  method stop () =
    if not is_waiting then
      begin
	decr(players_connected);
	if (!verbose_mode) then
	  print_endline ("Player n°" ^ string_of_int number ^ " has left the game.");
      end;
    Unix.close s_descr		
	       
  method run () =
    try
      while true do
	let command = my_input_line s_descr in
	let l = Str.split (Str.regexp "[/]") command in
	match List.nth l 0 with
	  "CONNECT" -> let name = String.sub command 8 (String.length command - 8) in
		       if (Hashtbl.mem !players_points name) then
			 let result = name ^ " is already taken by a player, try another one\n" in
			 ignore (Unix.write s_descr result 0 (String.length result));
		       else
			 begin
			   pseudo <- name;
			   let result = "CONNECTED/" ^ name ^ "\n" in
			   ignore (Unix.write s_descr result 0 (String.length result));
			   Hashtbl.add !players_points name 0
			 end;
	| "EXIT" -> let name = String.sub command 5 (String.length command - 5) in
		    if (Hashtbl.mem !players_points name) then
		      begin
			let result = "EXITED/" ^ name ^ "\n" in
			ignore (Unix.write s_descr result 0 (String.length result));
			Hashtbl.remove !players_points name
		      end
		    else
		      let result = name ^ "is not in the hashtable, it cannot be removed\n" in
		      ignore (Unix.write s_descr result 0 (String.length result));
	| "GUESS" -> let word = String.sub command 6 (String.length command - 6) in
		     let result = "GUESSED/" ^ word ^ pseudo ^ "\n" in
		     ignore (Unix.write s_descr result 0 (String.length result));
	| _ -> let result = command ^ " is unknown (try CONNECT/user/ or EXIT/user/)\n" in
	       ignore (Unix.write s_descr result 0 (String.length result));
      done;
    with
      exn -> print_string (Printexc.to_string exn);			  
end;;
  
class server port n =
object(s)
  val port_num = port
  val nb_pending = n
  val s_descr = Unix.socket Unix.PF_INET Unix.SOCK_STREAM 0
  method start () =
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
    while true do
      if (!players_connected = !max_players) & (!is_started != true) then
	begin
	  accept_players := false;
	  s#start_game
	end;
      let (service_sock, client_sock_addr) =
	Unix.accept s_descr
      in
      if (!players_connected != !max_players) & (!accept_players = true) then
	s#treat service_sock client_sock_addr false
      else
	begin
	  s#treat service_sock client_sock_addr true;
	end
    done
  method treat service_sock client_sock_addr waiting_player =
    ignore ((new connection service_sock client_sock_addr waiting_player)#start())
  method start_game =
    is_started := true;
    drawer := (Random.int (!max_players + 1)) + 1;
    if (!verbose_mode) then
      print_endline ("Player n°" ^ string_of_int !drawer ^ " is the drawer.");
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
    word := !dictionary_words.((Random.int (!dictionary_size)));
    if (!verbose_mode) then
      print_endline ("The drawer needs to draw the word \"" ^ !word ^ "\"");
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
