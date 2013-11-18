(*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *)

(* Game parameters *)
let max_players = ref 4
(*let incr_counter = let c = ref 0 in (fun () -> incr c; !c);;*)
let players_connected = ref 0
let timeout = ref 30
let port = ref 2013
let players_points = ref ((Hashtbl.create !max_players) : (string, int) Hashtbl.t)
let verbose_mode = ref false

let mutex_players = Mutex.create ()
let m = Mutex.create ()
let condition_players = Condition.create ()

let mutex_word = Mutex.create ()
let condition_word = Condition.create ()
let word_found = Condition.create ()

let mutex_proposition = Mutex.create ()
let new_proposition = Condition.create ()
let proposition = ref ""

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

let add_player player name = 
  if (!players_connected != !max_players) then
    begin
      Hashtbl.add !players_points name 0;
      Array.set !running_order !players_connected name;
      player#set_number_and_pseudo players_connected name;
      incr players_connected;
      true
    end
  else
    begin
      Mutex.unlock mutex_players;
      false
    end;;

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
    ignore (Thread.create (fun x ->
			   self#connection_player x;
			   self#send_roles_and_word x;
			   self#stop x
			  ) ());
    ignore (Thread.create (fun x ->
			   self#wait_drawing_proposition x;
			  ) ());
    ignore (Thread.create (fun x ->
			   self#wait_word_proposition x;
			  ) ());
    
  method stop () =
    if (pseudo != "") then
      begin
	decr players_connected;
	if (!verbose_mode) then
	  print_endline (pseudo ^ " has left the game.");
      end
    else
      print_endline ("Server had to kicked out a player because maximum capacity was reached.");
    Unix.close s_descr;
    Thread.exit ();
	       

  method connection_player () =
    try
      let command = my_input_line s_descr in
      let l = Str.split (Str.regexp "[/]") command in
      match List.nth l 0 with
      | "CONNECT" -> let name = String.sub command 8 (String.length command - 8) in
		     Mutex.lock mutex_players;
		     if (Hashtbl.mem !players_points name) then
		       begin
			 let result = "CONNECTION_REFUSED/" ^ name ^ "name_already_taken/\n" in
			 ignore (Unix.write s_descr result 0 (String.length result));
			 Mutex.unlock mutex_players
		       end			   
		     else
		       if (add_player self name) then
			 begin
			   let result = "CONNECTED/" ^ name ^ "\n" in
			   ignore (Unix.write s_descr result 0 (String.length result));
			   print_endline (name ^ " (n°" ^ (string_of_int number) ^ ") has just joined the game. " ^ string_of_int (!max_players - !players_connected) ^ " player(s) missing before starting the game.");
			   end
		       else
			 begin
			   let result = "CONNECTION_REFUSED/" ^ name ^ "maximum_capacity_reached/\n" in
			   ignore (Unix.write s_descr result 0 (String.length result));
			   raise Maximum_players_reached
			 end;
		     if (!players_connected = !max_players) then
		       begin
			 Mutex.unlock mutex_players;
			 Condition.signal condition_players;
			 Mutex.unlock m;			 
			 Condition.wait condition_word m;
			 print_endline ("passe !" ^ string_of_int (number));
		       end
		     else
		       begin
			 Mutex.unlock mutex_players;
			 Condition.wait condition_word m;
			 print_endline ("passe !" ^ string_of_int (number))
		       end
      | _ -> let result = command ^ " is unknown (try CONNECT/user/). \n" in
	     ignore (Unix.write s_descr result 0 (String.length result));
	     
    with
      Maximum_players_reached -> if (!verbose_mode) then
				   print_endline ("A player tried to join the game but maximum players capacity was reached.");
      | exn -> print_string (Printexc.to_string exn ^ "\n");
	       
  method set_number_and_pseudo n p =
    number <- !n;
    pseudo <- p;

  method send_roles_and_word () =
    print_endline (string_of_int (number) ^ " : round = " ^ string_of_int (!round));
    if (!round = number) then
      begin
	let result = "NEW_ROUND/drawer/" ^ !word ^ "/\n" in
	ignore (Unix.write s_descr result 0 (String.length result));
	if (!verbose_mode) then
	  print_endline ("The word " ^ !word ^ " has been sent to " ^ pseudo ^ ".");
      end
    else
      begin
	let result = "NEW_ROUND/finder/\n" in
	ignore (Unix.write s_descr result 0 (String.length result));
	if (!verbose_mode) then
	  print_endline (pseudo ^ "is a finder.");
      end;

  method wait_word_proposition () =
    print_endline "i waited for proposition";
(*    try
      while true do
	let command = my_input_line s_descr in
	let l = Str.split (Str.regexp "[/]") command in
	match List.nth l 0 with
	| "GUESS" -> let guessed_word = String.sub command 6 (String.length command - 6) in
		     Mutex.lock mutex_proposition;
		     proposition := guessed_word ^ "/" ^ pseudo;
		     Condition.broadcast new_proposition;
	| _ -> let result = command ^ " is unknown (try GUESS/word/)\n" in
	       ignore (Unix.write s_descr result 0 (String.length result));
      done;
    with exn -> print_string (Printexc.to_string exn ^ "\n");*)
		
  (*method send_propositions () =
    while (true) do
      Condition.wait new_proposition mutex_proposition;
      let result = "GUESSED/" ^ !proposition ^ "\n" in
      ignore (Unix.write s_descr result 0 (String.length result));
    done*)
		
  method wait_drawing_proposition () =
    print_endline "i waited for drawing";
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
    ignore (s#init_game ());
     while (true) do
      let (service_sock, client_sock_addr) =
	Unix.accept s_descr in
      s#treat service_sock client_sock_addr;
    done;
    
  method treat service_sock client_sock_addr =
    ignore ((new player service_sock client_sock_addr)#start())    

  method init_game () =
    Thread.create (fun x -> s#start_rounds x) ();

  method start_rounds () =
    if (!verbose_mode) then
      print_endline "The server is waiting for players.";
    Mutex.lock m;
    Condition.wait condition_players m;
    if (!verbose_mode) then
      print_endline "All players are now connected, let the game begin !";
    while (!round < !max_players) do
      if (!verbose_mode) then
	print_endline ("Round " ^ string_of_int (!round + 1) ^ "/" ^ string_of_int (!max_players) ^" !");
      if (!verbose_mode) then
	print_endline (Array.get !running_order !round ^ " is the drawer.");
      word := !dictionary_words.((Random.int (!dictionary_size)));
      Condition.broadcast condition_word;
      if (!verbose_mode) then
	print_endline ("The drawer needs to draw the word \"" ^ !word ^ "\".");
      Mutex.lock mutex_word;
      Condition.wait word_found m;
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
