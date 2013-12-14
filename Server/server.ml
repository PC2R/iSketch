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
let dictionary_words = ref []

let logfile = "log/server.log"

let registered_players = "db_players"

let word = ref ""
let word_found = ref false
let word_finders = ref 0

type role = DRAWER | FINDER

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

let timeout_on = ref false

let round_canceled = ref false

let cheat_counter = ref 0

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
  else if !score_round_drawer < 15 then
    score_round_drawer := !score_round_drawer + 1;
  let i = ref 0 in
  while (!i < List.length !players) do
    let player = List.nth !players !i in
    if player#get_role () = DRAWER then
      begin
	player#set_score_round !score_round_drawer;
	i := List.length !players
      end;
    i := !i + 1;
  done;
  incr word_finders;
  word_found := true;
  if !word_finders < !players_connected - 1 then
    begin
      notify_timeout ();
      timeout_on := true;
      thread_timeout := Thread.create (fun _ ->
				       Thread.delay (float_of_int !timeout);
				       if !timeout_on then
					 Condition.signal condition_end_round;
				       trace ("Timeout has just ended.")
				      ) ();
      trace ("Server has started the timeout.");
    end
  else
    begin
      timeout_on := false;
      Condition.signal condition_end_round
    end;;

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

let unescaped s =
  let string = ref "" in
  let i = ref 0 in
  while (!i < String.length s) do
    if String.sub s !i 1 = "\\" then
      begin
	if String.sub s (!i + 1) 1 = "/" then
	  begin
	    string := !string ^ "/";
	    i := !i + 1
	  end
	else if String.sub s (!i + 1) 1 = "\\" then
	  begin
	    string := !string ^ "\\";
	    i := !i + 1
	  end
	else
	  string := !string ^ (String.sub s !i 1);
      end
    else
      string := !string ^ (String.sub s !i 1);
    i := !i + 1;
  done;
  !string;;

let escaped s =
  let string = ref "" in
  let i = ref 0 in
  while (!i < String.length s) do
    if String.sub s !i 1 = "\\" then
	string := !string ^ "\\"
    else if String.sub s !i 1 = "/" then
      string := !string ^ "\\";
    string := !string ^ (String.sub s !i 1);
    i := !i + 1;
  done;
  !string;;		  

let exists_in_db name =
  let is_registered = ref false in
  let in_channel = open_in_gen[Open_creat] 0o666 registered_players in
  try
    while true do
      let line = (input_line in_channel) in
      let l = Str.split (Str.regexp " ") line in
      if List.nth l 0 = name then
	begin
	  is_registered := true;
	end
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
      let db_name = List.nth l 0
      and db_password = List.nth l 1
      and db_salt = List.nth l 2 in
      if db_name = name then
	if db_password = Digest.to_hex (Digest.string (password ^ db_salt)) then
	  valid := true;
    done;
    !valid;
  with End_of_file -> close_in in_channel;
		      !valid;;

let gen_salt n =
  let alphanum =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" in
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
      begin
	b := true;
      end
  done;
  !b;;

let generate_name n =
  let found = ref false
  and name = ref ""
  and i = ref 0 in
  while !found = false do
    i := !i + 1;
    name := (unescaped n) ^ string_of_int !i;
    if (not (exists !name)) && (not(exists_in_db !name)) then
      begin
	found := true;
      end;
  done;
  trace ("The name " ^ !name ^ " has been generated.");
  !name;;

let register_in_db name password =
  let salt = gen_salt 10 in
  let out_channel =
    open_out_gen [Open_append;Open_creat] 0o666 registered_players
  and md5sum_hexa = Digest.to_hex (Digest.string (password ^ salt)) in
  output_string out_channel (name ^ " " 
			     ^ md5sum_hexa ^ " " 
			     ^ salt ^ " " 
			     ^ string_of_int 0 ^ " " 
			     ^ string_of_int 0 ^ "\n");
  close_out out_channel;;

let update_statistics () =
  let winner = ref ""
  and losers = ref [] 
  and score = ref 0 in
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    if player#get_score_game () > !score then
      begin
	score := player#get_score_game ();
	winner := player#get_name ();
      end
  done;
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    if player#get_name () != !winner then
      losers := player#get_name ()::!losers;
  done;
  let in_channel = 
    open_in_gen[Open_creat] 0o666 registered_players
  and new_db = ref "" in
  try
    while true do
      let line = (input_line in_channel) in
      let l = Str.split (Str.regexp " ") line in
      let db_name = List.nth l 0
      and db_password = List.nth l 1
      and db_salt = List.nth l 2
      and db_wins = List.nth l 3
      and db_defeats = List.nth l 4 in
      if db_name = !winner then
	new_db := !new_db
		  ^ db_name ^ " "
		  ^ db_password ^ " "
		  ^ db_salt ^ " "
		  ^ string_of_int ((int_of_string db_wins) + 1) ^ " "
		  ^ db_defeats ^ "\n"
      else if List.mem db_name !losers then
	begin
	  losers := remove db_name !losers;
	  new_db := !new_db 
		    ^ db_name ^ " "
		    ^ db_password ^ " "
		    ^ db_salt ^ " "
		    ^ db_wins ^ " "
		    ^ string_of_int ((int_of_string db_defeats) + 1) ^ "\n";
	end
      else
	new_db := !new_db
		  ^ db_name ^ " "
		  ^ db_password ^ " "
		  ^ db_salt ^ " "
		  ^ db_wins ^ " "
		  ^ db_defeats ^ "\n";
    done;
  with End_of_file -> close_in in_channel;
		      let out_channel = open_out registered_players in
		      output_string out_channel !new_db;
		      close_out out_channel;;
  
let choose_word () =
  word := List.nth !dictionary_words
		   (Random.int (List.length !dictionary_words));
  trace ("The word " ^ !word ^ " has been chosen.");
  dictionary_words := remove !word !dictionary_words;;

let my_input_line file_descr =
  let s = " " and r = ref "" in
  while (Unix.read file_descr s 0 1 > 0) && s.[0] <> '\n' do
    r := !r ^ s;
  done;
  !r;;

let my_nth s n =
  let string = ref ""
  and i = ref 0
  and j = ref 0 in
  while (!j != n) do
    if String.sub s !i 1 = "/" then
      if String.sub s (!i - 1) 1 <> "\\" then
	j := !j + 1;
    i := !i + 1;
  done;
  while (!i < String.length s) do
    if (String.sub s !i 1 = "/") then
      begin
	if String.sub s (!i - 1) 1 <> "\\" then
	  i := String.length s
	else
	  begin
	    string := !string ^ (String.sub s !i 1);
	    i := !i + 1
	  end
      end
    else
      begin
	string := !string ^ (String.sub s !i 1);
	i := !i + 1;
      end
  done;
  !string;;

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
    player#send_command ("BROADCAST/" 
			 ^ name ^ " has reported cheating behavior./\n");
  done;;

let notify_talk name text =
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command ("LISTEN/" ^ name ^ "/" ^ text ^ "/\n");
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

let send_new_round_command name_drawer =
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    let name = player#get_name () in
    let result = "NEW_ROUND/" ^ name_drawer ^ "/" ^ name ^ "/" ^ !word ^ "/\n" in
    player#send_command result
  done;;

let choose_drawer () =
  let j = ref 0
  and i = ref !round in
  while (!i < List.length !players) do
    let player = List.nth !players !round in
    if player#get_status () != false then
      begin
	player#set_role DRAWER;
	trace ("The drawer for the round n°" ^ (string_of_int (!round + 1))
	       ^ " is " ^ player#get_name () ^ ".");
	j := !i;
	i := List.length !players
      end
    else
      i := !i + 1;
  done;
  !j;;

let send_score_round_command () =
  let result = ref "SCORE_ROUND/" in
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    result := !result ^ player#get_name () ^ "/"
	      ^ string_of_int (player#get_score_round ()) ^ "/";
    player#update_score_game;
  done;
  result := !result ^ "\n";
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#send_command !result
  done;;
  
let send_end_round_command () =
  if !round_canceled then
    for i = 0 to (List.length !players - 1) do
      (List.nth !players i)#send_command ("END_ROUND//" ^ !word ^ "/\n")
    done
  else
    begin
      let score = ref 0
      and name = ref "" in
      for i = 0 to (List.length !players - 1) do
	let player1 = List.nth !players i in
	if (player1#get_role () != DRAWER)
	   && (player1#get_score_round () > !score) then
	  begin
	    score := player1#get_score_round ();
	    name := player1#get_name ()
	  end
	else if player1#get_role () = DRAWER then
	  player1#set_role FINDER
      done;
      for i = 0 to (List.length !players - 1) do
	let player2 = List.nth !players i in
	player2#send_command ("END_ROUND/" ^ !name ^ "/" ^ !word ^ "/\n")
      done
    end;;
    
let reset_score_players () =
  score_round_drawer := 0;
  for i = 0 to (List.length !players - 1) do
    let player = List.nth !players i in
    player#set_score_round 0;
  done;;

class player pseudo s_descr =
object (self)

  val s_descr = s_descr
  val mutable name = ""
  val mutable score_round = 0
  val mutable score_game = 0
  val mutable connected = true
  val mutable role = FINDER

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
		   let guessed_word = my_nth command 1 in
		   if (unescaped guessed_word) = !word then
		     begin
		       notify_word_found name;
		       score_round <- !score_round_finder;
		       update_variables ()
		     end
		   else
		     notify_guess guessed_word name;
		   Mutex.unlock mutex_guessed_word;
      | "SET_COLOR" -> let new_color = 
			 String.sub command 10 (String.length command - 10) in
		       rgb := new_color;
		       trace (name ^ " has just changed the color of the line.");
      | "SET_SIZE" -> let new_size =
			String.sub command 9 (String.length command - 9) in
		      size := new_size;
		      trace (name ^ " has just changed the color of the line.");
      | "SET_LINE" -> let new_line =
			String.sub command 9 (String.length command - 9) in
		      trace (name ^ " has just proposed a line.");
		      notify_line new_line;
      | "EXIT" -> let name = my_nth command 1 in
		  connected <- false;
		  decr players_connected;
		  notify_exit name;
      | "CHEAT" -> let name = my_nth command 1 in
		   cheat_counter := !cheat_counter + 1;
		   trace ((unescaped name) ^ " has reported cheating behavior.");
		   notify_cheat name;
		   if !cheat_counter = !cheat_parameter then
		     begin
		       timeout_on := false;
		       reset_score_players ();
		       Condition.signal condition_end_round
		     end;
      | "PASS" -> if !word_found = true then
		    begin
		      reset_score_players ();
		      timeout_on := false;
		    end;
		  Condition.signal condition_end_round;
      | "TALK" -> let text = List.nth l 1 in
		  notify_talk name text;
      | _ -> trace (command ^ "has been received from " ^ name ^ ".");
    done;

  method get_name () = name;
  method get_score_round () = score_round;
  method get_score_game () = score_game;
  method get_status () = connected;
  method get_role () = role;
  method set_role r = role <- r;
  method set_score_round s = score_round <- s;

  method update_score_game =
    score_game <- score_game + score_round;
    score_round <- 0;

  method send_command result =
    ignore (Unix.write s_descr result 0 (String.length result));
    trace (String.sub result 0 (String.length result - 1)
	   ^ " has been sent to " ^ name ^ ".");

end;;

let welcome_player name s_descr =
  let result = "WELCOME/" ^ name ^ "/\n" in
  ignore (Unix.write s_descr result 0 (String.length result));
  let player = new player (unescaped name) s_descr in
  players := player::!players;
  incr players_connected;
  trace (unescaped (name)
	 ^ " has been successfully welcomed to the game.");
  if ((List.length !players) = !max_players) then
    begin
      Condition.signal condition_players;
      Mutex.unlock mutex_maximum_players;
    end;;

let connection_player (s_descr, sock_addr) =
  try
    let command = my_input_line s_descr in
    let l = Str.split (Str.regexp "[/]") command in
    match List.nth l 0 with
    | "CONNECT" -> Mutex.lock mutex_players;
		   let name = ref (my_nth command 1) in
		   if ((List.length !players) = !max_players) then
		     begin
		       let r = "ACCESSDENIED/"
			       ^ !name ^ "maximum_capacity_reached/\n" in
		       ignore (Unix.write s_descr r 0 (String.length r));
		       trace (unescaped (!name)
			      ^ " tried to join the game \
				 but maximum capacity was reached.")
		     end
		   else
		     begin
		       if (exists (unescaped (!name)) 
			   or exists_in_db (unescaped (!name))) then
			 begin
			   name := generate_name !name;	
			 end;       
		       welcome_player !name s_descr
		     end;
		   Mutex.unlock mutex_players;
		   Thread.exit ()
    | "REGISTER" -> Mutex.lock mutex_players;
		    let name = my_nth command 1
		    and password = my_nth command 2 in
		    if (exists_in_db name) then
		      let result = "ACCESSDENIED/\n" in
		      ignore (Unix.write s_descr result 0
					 (String.length result));
		      trace ((unescaped name)
			     ^ " has tried to register \
				but username was already in the database.");
		    else
		      begin
			register_in_db (unescaped name) password;
			trace ((unescaped name)
			       ^ " has been successfully \
				  registered to the game.");
			welcome_player name s_descr
		      end;
		    Mutex.unlock mutex_players;
		    Thread.exit ()
    | "LOGIN" -> Mutex.lock mutex_players;
		 let name = my_nth command 1
		 and password = my_nth command 2 in
		 if is_ok (unescaped name) password = false then
		   let result = "ACCESSDENIED/\n" in
		   ignore (Unix.write s_descr result 0 (String.length result));
		   trace ((unescaped name)
			 ^ " has tried to log in but it failed.");
		 else
		   begin
		     if (exists (unescaped name)) then
		       begin
			 let result = "ACCESSDENIED/\n" in
			 ignore (Unix.write s_descr result 0
					    (String.length result));
			 trace ((unescaped name)
				^ " has tried to log in but \
				   was already connected.")
		       end
		     else
		       begin
			 trace ((unescaped name)
				^ " has been successfully logged in.");
			 welcome_player name s_descr
		       end
		   end;
		 Mutex.unlock mutex_players;
		 Thread.exit ()
    | _ -> trace (command ^ "has been received."); 
  with
  | exn -> trace (Printexc.to_string exn)
	 
class server port n =
object (self)

  val port_num = port
  val nb_pending = n
  val s_descr = Unix.socket Unix.PF_INET Unix.SOCK_STREAM 0

  initializer
  let host = Unix.gethostbyname (Unix.gethostname()) in
      let h_addr = host.Unix.h_addr_list.(0) in
  (*let h_addr = Unix.inet_addr_any in*)
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
    let i = ref 0 in
    while (!round < !max_players ) do
      trace ("Round " ^ string_of_int (!round + 1) ^ "/"
	     ^ string_of_int (!max_players) ^" has just begun.");
      init_variables ();
      choose_word ();
      i := choose_drawer ();
      send_new_round_command ((List.nth !players !i)#get_name ());
      Condition.wait condition_end_round mutex_end_round;
      send_end_round_command ();
      send_score_round_command ();
      incr round;
    done;
    update_statistics ();

end;;

let rec read_db in_channel =
  let result = ref "<ul>" in
  try
    while true do
      let line = (input_line in_channel) in
      let l = Str.split (Str.regexp " ") line in
      result := !result ^ "<li>" ^ List.nth l 0 
		^ " has " ^ List.nth l 3 ^ " wins and " 
		^ List.nth l 4 ^ " defeats.</li>\n";
    done;
    !result;
  with End_of_file -> close_in in_channel;
		      result := !result ^ "\n</ul>\n</body>\n</html>";
		      !result;;


let generate_response protocol =
  let result = ref ("HTTP/" ^ protocol ^ " 200 OK\r\n" 
	       ^ "Content-Type: text/html; charset=UTF-8\r\n\r\n" 
	       ^ "<!DOCTYPE html>\n"
	       ^ "<html lang=\"fr\">\n"
	       ^ "<head>\n"
	       ^ "<meta http-equiv=\"Content-Type\" \
		  content=\"text/html; charset=utf-8\" />\n"
	       ^ "<meta http-equiv=\"Refresh\" content=\"60\">\n"
	       ^ "<title>Statistics of the game</title>\n"
	       ^ "</head>\n") in
  result := !result ^ "<body>\n<h1>Hello World !</h1>\n";
  result := !result ^ "<p>Players : </p>\n";
  let in_channel = open_in_gen[Open_creat] 0o666 registered_players in
  result := !result ^ (read_db in_channel);
  !result;;
	 
  
let response (s_descr, sock_addr) =
  try
    let command = my_input_line s_descr in
    let l = Str.split (Str.regexp "[/]") command in
    match List.nth l 0 with
    | "GET " -> let protocol = List.nth l 2 in
		let result = generate_response protocol in
		ignore (Unix.write s_descr result 0 (String.length result));
		Unix.shutdown s_descr Unix.SHUTDOWN_ALL;
    | _ -> trace (command ^ "has been received on serverHTTP."); 
  with
  | exn -> trace (Printexc.to_string exn);;

class serverHTTP port n =
object(self)

  val port_num = port
  val nb_pending = n
  val s_descr = Unix.socket Unix.PF_INET Unix.SOCK_STREAM 0
			    
  initializer
  let host = Unix.gethostbyname (Unix.gethostname()) in
      let h_addr = host.Unix.h_addr_list.(0) in
      (*let h_addr = Unix.inet_addr_any in*)
      let sock_addr = Unix.ADDR_INET (h_addr, port) in
      Unix.setsockopt s_descr Unix.SO_REUSEADDR true;
      Unix.bind s_descr sock_addr;
      Unix.listen s_descr n;
      trace ("Server HTTP successfully started.")

  method start () =
    while (true) do
      let (service_sock, client_sock_addr) =
	Unix.accept s_descr in
      ignore (Thread.create response (service_sock, client_sock_addr));
    done;

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
  ignore (Thread.create (fun x -> (new server !port 2)#wait_connections ()) ());
  (new serverHTTP 2092 2)#start ();;

  main ();;
