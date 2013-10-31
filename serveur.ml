let max_players = ref 4
let timeout = ref 30
let port = ref 2013
let players = ref ((Hashtbl.create !max_players) : (string, int) Hashtbl.t)
		  
let establish_server service port =
  (* we create a TCP/IP socket... *)
  let s_descr = Unix.socket Unix.PF_INET Unix.SOCK_STREAM 0
  (* ... and an address *)
  and sockaddr = Unix.inet_addr_of_string "127.0.0.1" in
  (* this option allows to stop our server and restart it
  immediatly *)
  Unix.setsockopt s_descr Unix.SO_REUSEADDR true;
  (* we bind the socket to the address *)
  Unix.bind s_descr (Unix.ADDR_INET(sockaddr, port));
  (* the second argument to the listen function gives the maximum
  number of connections *)
  print_endline ("Server listening on port " ^ string_of_int port);
  Unix.listen s_descr 2;
  while true do
    try
      let (s, caller) = Unix.accept s_descr in
      match Unix.fork() with
	(* Unix.fork() returns 0 to the child process *)
	0 -> if Unix.fork () <> 0 then
	       begin
		 exit 0;
	       end
	     else
	       print_endline "Client connected";
	     let inchan = Unix.in_channel_of_descr s
	     and outchan = Unix.out_channel_of_descr s in
	     print_endline "?";
	     output_string outchan "coucou CLara";
	     flush outchan;
	     service inchan outchan;
	     close_in inchan;
	     close_out outchan;
	     exit 0;
	(* parent *)
	| pid -> Unix.close s;
		 ignore (Unix.waitpid [] pid);
    with End_of_file -> print_endline "Client disconnected"
  done

let test in_channel out_channel =
  output_string out_channel "a";
  flush out_channel;
  while (true) do
    let command = input_line in_channel in
    output_string out_channel command;
    flush out_channel;
(*    let l = Str.split (Str.regexp "[/]") command in
    begin
      output_string out_channel command;
      flush out_channel;
      match List.nth l 0 with
      (* 8 = size of "CONNECT/" *)
	    "CONNECT" -> let name = String.sub command 8 (String.length command - 8) in
			 if (Hashtbl.mem !players name) then
			   print_endline (name ^ " is already taken by another player, try another one")
			 else
			   begin
			     print_endline (name ^ " added successfuly to the hashtable");
			     Hashtbl.add !players name 0;
			   end
    (* 5 = size of "EXIT/" *)
	  | "EXIT" -> let name = String.sub command 5 (String.length command - 5) in
		      Hashtbl.remove !players name;
		      print_endline (name ^ "removed successfuly from the hashtable");
	  | _ -> print_endline "error";
    end*)
  done
    
let main =
  begin
    let speclist =
      [("-max", Arg.Set_int max_players, "Sets maximum number of players (default : 4)");
       ("-timeout", Arg.Set_int timeout, "Sets the length of the timeout (in seconds) when the word is found (default : 30)");
       ("-port", Arg.Set_int port, "Sets the listening port for the server (default : 2013)");
      ]
    in let usage_msg = "Options availables for iSketch server :"
       in Arg.parse speclist print_endline usage_msg;
  end;
  establish_server test !port;;
  (*print_endline (string_of_int (Hashtbl.length (!players)));
  test "CONNECT/gandalf/";
  test "CONNECT/saroumane/";
  test "CONNECT/avec\\backslash/"; (* avec\backslash *)
  test "CONNECT/avec\\/slash/"; (* avec/slash *)
  test "CONNECT/saroumane/";
  test "EXIT/gandalf/";
  test "EXIT/saroumane/";
  print_endline (string_of_int (Hashtbl.length (!players)));;*)
  
let () = main
