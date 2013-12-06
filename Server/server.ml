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

let max_players = ref 4
let timeout = ref 30
let port = ref 2013
let cheat_parameter = ref 3 (* should be (max_players - 1) by default but subject says 3 *)
let verbose_mode = ref false

let dictionary_filename = ref "dictionary"
let logfile = "log/server.log"
let registered_players = "db_players"

let dictionary_words = ref []

let word = ref ""
let word_found = ref false
let word_finders = ref 0

type role = DRAWER | FINDER | NOT_DEFINED

let players = ref []

let mutex_players = Mutex.create ()
let mutex_maximum_players = Mutex.create ()

let condition_players = Condition.create ()

let condition_end_round = Condition.create ()
let mutex_end_round = Mutex.create ()

let mutex_guessed_word = Mutex.create ()

let players_connected = ref 0

let round = ref 0

let rgb = ref "0/0/0/"
let size = ref "1/"
let line = ref ""

let score_round_finder = ref 0
let score_round_drawer = ref 0

let back_up = ref ""

let timeout_on = ref false

let thread_timeout = ref (Thread.create (fun _ ->
					 while true do
					   Thread.yield ()
					 done;
					) ()
			 )

let trace message =
  let out_channel = open_out_gen [Open_append;Open_creat] 0o666 logfile
  and date = Unix.gmtime (Unix.time ()) in
  output_string out_channel
		("[" ^ string_of_int (date.Unix.tm_mday)
		 ^ "/" ^ string_of_int (date.Unix.tm_mon + 1)
		 ^ "/" ^ string_of_int (date.Unix.tm_year + 1900)
		 ^ " " ^ string_of_int (date.Unix.tm_hour)
		 ^ ":" ^ string_of_int (date.Unix.tm_min)
		 ^ ":" ^ string_of_int (date.Unix.tm_sec)
		 ^ " +0100]" ^ " \"" ^ message ^ "\"\n");
  if (!verbose_mode) then
    print_endline message;
  close_out out_channel;;

let notify_timeout () =
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command ("WORD_FOUND_TIMEOUT/" ^ string_of_int !timeout ^ "/\n");
  done;;

let update_variables () =
  if !score_round_finder > 5 then score_round_finder := !score_round_finder -1;
  if !score_round_drawer = 0 then score_round_drawer := 10
  else
    if !score_round_drawer < 15 then
      score_round_drawer := !score_round_drawer + 1;
  if !word_found = false then
    begin
      notify_timeout ();
      word_found :=true;
      timeout_on := true;
      thread_timeout := Thread.create (fun _ ->
				       Thread.delay (float_of_int !timeout);
				       if !timeout_on then
					 Condition.signal condition_end_round;
				       trace ("Timeout has just ended.")
				      ) ();
      trace ("Server has started the timeout.");
    end;
  incr word_finders;
  if !word_finders = !players_connected - 1 then
    begin
      timeout_on := false;
      Condition.signal condition_end_round
    end;;

let cheat_counter = ref 0

let init_variables () =
  word_found := false;
  word_finders := 0;
  score_round_finder := 10;
  score_round_drawer := 0;
  cheat_counter := 0;;

let rec remove element list =
  match list with
  | [] -> []
  | head::tail -> if head = element then
		    tail
		  else
		    head::(remove element tail);;

let rec read_file in_channel =
  try 
    let word = (input_line in_channel) in
    word::(read_file in_channel)
  with End_of_file -> close_in in_channel;
		      [];;

let init_dict () =
  let in_channel = open_in !dictionary_filename in
  read_file in_channel;;

let exists_in_db name =
  let is_registered = ref false in
  let in_channel = open_in_gen[Open_creat] 0o666 registered_players in
  try
    while true do
      let line = (input_line in_channel) in
      let l = Str.split (Str.regexp " ") line in
      if List.nth l 0 = name then
	is_registered := true;
    done;
    !is_registered;
  with End_of_file -> close_in in_channel;
		      !is_registered;;

let is_ok name password =
  let valid = ref false in
  let in_channel = open_in_gen[Open_creat] 0o666 registered_players in
  try
    while true do
      let line = (input_line in_channel) in
      let l = Str.split (Str.regexp " ") line in
      if List.nth l 0 = name then
	if List.nth l 1 = Digest.to_hex (Digest.string password) then
	  valid := true;
    done;
    !valid;
  with End_of_file -> close_in in_channel;
		      !valid;;

let gen_salt n =
  let alphanum = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" in
  let length = String.length alphanum in
  let str = String.create n in        
  for i = 0 to pred n do
    str.[i] <- alphanum.[Random.int length]
  done;
  (str) ;;

let exists name =
  let b = ref false in
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    if player#get_name () = name then
      b := true
  done;
  !b;;

let generate_name n =
  let found = ref false
  and name = ref n
  and i = ref 1 in
  while !found = false do
    name := n ^ string_of_int !i;
    if exists !name = false then
      found := true;
    i := !i + 1;
  done;
  !name;;

let register_in_db name password =
  let salt = gen_salt (String.length (name ^ password)) in
  let out_channel = open_out_gen [Open_append;Open_creat] 0o666 registered_players
  and md5sum_hexa = Digest.to_hex (Digest.string password ^ salt) in
  output_string out_channel (name ^ " " ^ md5sum_hexa ^ " " ^ salt ^ "\n");
  close_out out_channel;;
  
let choose_word () =
  word := List.nth !dictionary_words (Random.int (List.length !dictionary_words));
  trace ("The word " ^ !word ^ " has been chosen.");
  dictionary_words := remove !word !dictionary_words;;

let my_input_line file_descr =
  let s = " " and r = ref "" in
  while (Unix.read file_descr s 0 1 > 0) && s.[0] <> '\n' do
    r := !r ^ s;
  done;
  !r;;

(* function for EXITED / GUESSED / WORD_FOUND *)
let notify_players keyword name =
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command (keyword ^ "/" ^ name ^ "/\n");
  done;;

let notify_exit name =
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command ("EXITED/" ^ name ^ "\n");
  done;;

let notify_guess word name =
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command ("GUESSED/" ^ word ^ "/" ^ name ^ "/\n");
  done;;

let notify_word_found name = 
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command ("WORD_FOUND/" ^ name ^ "/\n");
  done;;

let notify_line line =
  for i = 0 to (List.length !players - 1) do
    let player =  List.nth !players i in
    player#send_command ("LINE/" ^ line ^ !rgb ^ !size ^ "\n");
  done;;

let notify_cheat name =
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command ("BROADCAST/" ^ name ^ " has reported cheating behavior./\n");
  done;;

let notify_talk name text =
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command ("LISTEN/" ^ name ^ text ^ "/\n");
  done;;
  
let send_connected_command () =
  for i = 0 to (List.length !players - 1) do
    let player1 = List.nth !players i in
    let name = player1#get_name () in
    for j = 0 to (List.length !players - 1) do
      let player2 = List.nth !players j in
      player2#send_command ("CONNECTED/" ^ name ^ "/\n");
    done;
  done;;

let send_new_round_command () =
  let player = List.nth !players !round in
  if player#get_status () != false then
    begin
      player#set_role "drawer";
      let name = player#get_name () in
      for i = 0 to (List.length !players - 1) do
	let player2 = List.nth !players i in
	let name2 = player2#get_name () in
	let result = "NEW_ROUND/" ^ name ^ "/" ^ name2 ^ "/" ^ !word ^ "/\n" in
	player2#send_command result
      done;
    end;;

let send_score_round_command () =
  let result = ref "SCORE_ROUND/" in
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    if (player#get_role () = "drawer")then
      result := !result ^ player#get_name () ^ "/" ^ string_of_int (!score_round_drawer) ^ "/"
    else
      result := !result ^ player#get_name () ^ "/" ^ string_of_int (player#get_score ()) ^ "/"
  done;
  result := !result ^ "\n";
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command !result
  done;;
  
let send_end_round_command () =
  let score = ref 0 and name = ref "" in
  for i = 0 to (List.length !players - 1) do
    let player1 = List.nth !players i in
    if (player1#get_score () > !score) then
      begin
	score := player1#get_score ();
	name := player1#get_name ()
      end
  done;
  for i = 0 to (List.length !players - 1) do
    let player2 = List.nth !players i in
    if !name = "" then
      player2#send_command ("END_ROUND/" ^ "/" ^ !word ^ "/\n")
    else
      player2#send_command ("END_ROUND/" ^ !name ^ "/" ^ !word ^ "/\n")
  done;;
    
let reset_score_players () =
  score_round_drawer := 0;
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#set_score 0;
  done;;

class player pseudo s_descr =
object (self)

  val s_descr = s_descr
  val mutable name = ""
  val mutable score = 0
  val mutable connected = true
  val mutable role = ""

  initializer
    name <- pseudo;
    ignore (Thread.create self#start_game ());
 
  method start_game () =
    (* to do *)
    (* wait every one before starting to send guess command *)
    while (true) do
      let command = my_input_line s_descr in
      let l = Str.split (Str.regexp "[/]") command in
      match List.nth l 0 with
      | "GUESS" -> Mutex.lock mutex_guessed_word;
		   let guessed_word = List.nth l 1 in
		   if guessed_word = !word then
		     begin
		       notify_word_found name;
		       score <- !score_round_finder;
		       update_variables ()
		     end
		   else
		     notify_guess guessed_word name;
		   Mutex.unlock mutex_guessed_word;
      | "SET_COLOR" -> let new_color = String.sub command 10 (String.length command - 10) in
		       rgb := new_color;
		       trace (name ^ " has just changed the color of the line.");
      | "SET_SIZE" -> let new_size = String.sub command 9 (String.length command - 9) in
		      size := new_size;
		      trace (name ^ " has just changed the color of the line.");
      | "SET_LINE" -> let new_line = String.sub command 9 (String.length command - 9) in
		      trace (name ^ " has just proposed a line.");
		      notify_line new_line;
      | "EXIT" -> let name = String.sub command 5 (String.length command - 5) in
		  connected <- false;
		  decr players_connected;
		  notify_exit name;
      | "CHEAT" -> cheat_counter := !cheat_counter + 1;
		   trace (name ^ " has reported cheating behavior.");
		   notify_cheat name;
		   if !cheat_counter = !cheat_parameter then
		     trace (string_of_int (!cheat_parameter) ^ " players have reported cheating behavior.");
      | "PASS" -> if !word_found = true then
		    begin
		      reset_score_players ();
		      timeout_on := false;
		    end;
		  Condition.signal condition_end_round;
      | "TALK" -> let text = String.sub command 5 (String.length command - 5) in
		  notify_talk name text;
      | _ -> trace(command ^ "has been received from " ^ name ^ ".");
    done;


  method get_name () = name;
  method get_score () = score;
  method get_status () = connected;
  method get_role () = role;
  method set_role r = role <- r;
  method set_score s = score <- s;

  method send_command result =
    ignore (Unix.write s_descr result 0 (String.length result));
    trace (String.sub result 0 (String.length result - 1) ^ " has been sent to " ^ name ^ ".");

end;;

let connection_player (s_descr, sock_addr) =
  try
    let command = my_input_line s_descr in
    let l = Str.split (Str.regexp "[/]") command in
    match List.nth l 0 with
    | "CONNECT" -> Mutex.lock mutex_players;
		   let name = ref (List.nth l 1) in
		   if ((List.length !players) = !max_players) then
		     begin
		       let result = "CONNECTION_REFUSED/"
				    ^ !name ^ "maximum_capacity_reached/\n" in
		       ignore (Unix.write s_descr result 0 (String.length result));
		       trace (!name
			      ^ " tried to join the game but maximum capacity was reached.");
		       Mutex.unlock mutex_players;
		       Thread.exit ()
		     end
		   else
		     begin
		       if (exists !name) then
			 name := generate_name !name;
		       let result = "WELCOME/" ^ !name ^ "/\n" in
		       ignore (Unix.write s_descr result 0 (String.length result));
		       let player = new player !name s_descr in
		       players := player::!players;
		       incr players_connected;
		       trace (!name
			      ^ " has been successfully welcomed to the game.");
		       if ((List.length !players) = !max_players) then
			 begin
			   Condition.signal condition_players;
			   Mutex.unlock mutex_maximum_players;
			 end;
		       Mutex.unlock mutex_players;
		       Thread.exit ()
		     end
    | "REGISTER" -> Mutex.lock mutex_players;
		    let name = List.nth l 1
		    and password = List.nth l 2 in
		    if (exists_in_db name) then
		      let result = "ACCESSDENIED/\n" in
		      ignore (Unix.write s_descr result 0 (String.length result));
		      trace (name
			     ^ "has tried to register but username was already in the database.");
		      Mutex.unlock mutex_players;
		      Thread.exit ()
		    else
		      register_in_db name password;
    | "LOGIN" -> Mutex.lock mutex_players;
		 let name = List.nth l 1
		 and password = List.nth l 2 in
		 if is_ok name password = false then
		   let result = "ACCESSDENIED/\n" in
		   ignore (Unix.write s_descr result 0 (String.length result));
		   trace (name
			 ^ "has tried to log in but it failed.");
		   Mutex.unlock mutex_players;
		   Thread.exit ();
    | _ -> let result = command ^ " is unknown (try CONNECT/user/). \n" in
	   ignore (Unix.write s_descr result 0 (String.length result))	   
  with
  | exn -> trace (Printexc.to_string exn)

class server port n =
object (self)

  val port_num = port
  val nb_pending = n
  val s_descr = Unix.socket Unix.PF_INET Unix.SOCK_STREAM 0

  initializer
  (*let host = Unix.gethostbyname (Unix.gethostname()) in
      let h_addr = host.Unix.h_addr_list.(0) in*)
  let h_addr = Unix.inet_addr_any in
      let sock_addr = Unix.ADDR_INET (h_addr, port) in
      Unix.setsockopt s_descr Unix.SO_REUSEADDR true;
      Unix.bind s_descr sock_addr;
      Unix.listen s_descr n;
      dictionary_words := init_dict ();      
      trace ("Server successfully started with parameters : "
	     ^ string_of_int !max_players ^ " maximum players and "
	     ^ string_of_int !timeout ^ " seconds of timeout.");
      trace ("Server is waiting for players connections on port "
	     ^ string_of_int port ^ " and host/address "
	     ^ Unix.gethostname() ^ "/"
	     ^ Unix.string_of_inet_addr h_addr ^ ".");
      trace ("Server has successfully read the dictionary.");
    
      
  method wait_connections () =
    ignore (Thread.create self#start_game ());
    while (true) do
      let (service_sock, client_sock_addr) =
	Unix.accept s_descr in
      ignore (Thread.create connection_player (service_sock, client_sock_addr));
    done;

  method start_game () =
    Condition.wait condition_players mutex_maximum_players;
    trace ("All players are now connected, let the game begin !");
    send_connected_command ();
    while (!round < !max_players ) do
      trace ("Round " ^ string_of_int (!round + 1) ^ "/" ^ string_of_int (!max_players) ^" has just begun.");
      init_variables ();
      choose_word ();
      send_new_round_command ();
      Condition.wait condition_end_round mutex_end_round;
      send_end_round_command ();
      send_score_round_command ();
      incr round;
    done

end;;

let main () =
  begin
    let speclist =
      [("-max", Arg.Set_int max_players,
	" Sets maximum number of players (default : 4)");
       ("-timeout", Arg.Set_int timeout,
	" Sets the length of the timeout (in seconds)
	 when the word is found (default : 30)");
       ("-port", Arg.Set_int port,
	" Sets the listening port for the server (default : 2013)");
       ("-dico", Arg.Set_string dictionary_filename,
	" Dictionary");
       ("-n", Arg.Set_int cheat_parameter,
       " Sets the cheat parameter (default : 3)");
       ("-v", Arg.Set verbose_mode,
	" Enables verbose mode");
      ]
    in let usage_msg = "Options availables for iSketch server :"
       in Arg.parse speclist print_endline usage_msg;
  end;
  Random.self_init();
  (new server !port 2)#wait_connections ();;

  main ();;
