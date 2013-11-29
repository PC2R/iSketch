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
let verbose_mode = ref false

let dictionary_filename = ref "dictionary"
let logfile = "log/server.log"

let dictionary_words = ref (Array.make 0 "")

let players = ref []

let mutex_players = Mutex.create ()
let players_connected = ref 0

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

let init_dict () =
  let lines =
    let n = ref 0
    and in_channel = open_in !dictionary_filename in
    let rec loop () =
      ignore (input_line in_channel); incr n; loop ()
    in
    try loop () with End_of_file -> close_in in_channel; !n in
  dictionary_words := Array.append !dictionary_words (Array.make lines "");
  let in_channel = open_in !dictionary_filename and i = ref 0 in
  let rec loop () =
    Array.set !dictionary_words !i (input_line in_channel); incr i; loop ()
  in
  try loop () with End_of_file -> close_in in_channel;;

let my_input_line file_descr =
  let s = " " and r = ref "" in
  while (Unix.read file_descr s 0 1 > 0) && s.[0] <> '\n' do
    r := !r ^ s
  done;
  !r;;

class player pseudo s_descr =
object (self)

  val s_descr = s_descr
  val mutable name = ""
  val mutable id = 0
  val mutable role = ""

  initializer
    name <- pseudo;

  method start () =
    print_endline ("hey");

  method get_name () =
    name;

end;;

let connection_player (s_descr, sock_addr) =
  try
    let command = my_input_line s_descr in
    let l = Str.split (Str.regexp "[/]") command in
    match List.nth l 0 with
    | "CONNECT" -> let name = String.sub command 8 (String.length command - 8) in
		   Mutex.lock mutex_players;
		   if ((List.length !players) = !max_players) then
		     begin
		       let result = "CONNECTION_REFUSED/"
				    ^ name ^ "maximum_capacity_reached/\n" in
		       ignore (Unix.write s_descr result 0 (String.length result));
		       Mutex.unlock mutex_players;
		       Thread.exit ()
		     end
		   else
		     begin
		       for i = 0 to (List.length !players - 1) do
			 if (List.nth !players i)#get_name () = name then
			   begin
			     let result = "CONNECTION_REFUSED/"
					  ^ name ^ "name_already_taken/\n" in
			     ignore (Unix.write s_descr result 0 (String.length result));
			     Mutex.unlock mutex_players;
			     Thread.exit ()
			   end
		       done;
		       let result = "CONNECTED/" ^ name ^ "\n" in
		       ignore (Unix.write s_descr result 0 (String.length result));
		       let player = new player name s_descr in
		       players := player::!players;
		       incr players_connected;
		       Mutex.unlock mutex_players;
		       Thread.exit ()
		     end
    | _ -> let result = command ^ " is unknown (try CONNECT/user/). \n" in
	   ignore (Unix.write s_descr result 0 (String.length result));
  with
  | exn -> trace (Printexc.to_string exn);

class server port n =
object (self)

  val port_num = port
  val nb_pending = n
  val s_descr = Unix.socket Unix.PF_INET Unix.SOCK_STREAM 0

  initializer
  let host = Unix.gethostbyname (Unix.gethostname()) in
      let h_addr = host.Unix.h_addr_list.(0) in
      let sock_addr = Unix.ADDR_INET (h_addr, port) in
      Unix.setsockopt s_descr Unix.SO_REUSEADDR true;
      Unix.bind s_descr sock_addr;
      Unix.listen s_descr n;
      trace ("Server successfully started with parameters : "
	     ^ string_of_int !max_players ^ " maximum players and "
	     ^ string_of_int !timeout ^ " seconds of timeout.\n"
	     ^ "Server is waiting for players connections on port "
	     ^ string_of_int port ^ " and host/address "
	     ^ Unix.gethostname() ^ "/"
	     ^ Unix.string_of_inet_addr h_addr ^ ".");
      init_dict ();
      
  method start () =
    ignore (Thread.create self#game ());
    while (true) do
      let (service_sock, client_sock_addr) =
	Unix.accept s_descr in
      ignore (Thread.create connection_player (service_sock, client_sock_addr));
    done;

  method game () =
    init_dict ();
    Thread.delay (30.);
    for i = 0 to (List.length !players - 1) do
      print_endline ((string_of_int i) ^ " : ");
      print_endline ((List.nth !players i)#get_name ());
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
       ("-v", Arg.Set verbose_mode,
	" Enables verbose mode");
      ]
    in let usage_msg = "Options availables for iSketch server :"
       in Arg.parse speclist print_endline usage_msg;
  end;
  Random.self_init();
  (new server !port 2)#start();;

  main ();;
