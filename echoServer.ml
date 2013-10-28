(* Echo Server *)
(* running the client-server : $ ./echoServer port *)
(* starting the client : $ telnet localhost port *)

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
	     service inchan outchan;
	     close_in inchan;
	     close_out outchan;
	     exit 0;
	(* parent *)
	| pid -> Unix.close s;
		 ignore (Unix.waitpid [] pid);
    with End_of_file -> print_endline "Client disconnected"
  done


let echo_service in_channel out_channel =
  output_string out_channel "Press enter to quit the client\n";
  flush out_channel;
  try while (true) do
	let line = input_line in_channel in
	(* \013 = enter to exit the client *)
	if (line = "") or (line = "\013") then
	  raise Exit
	else
	  output_string out_channel (line^"\n");
	flush out_channel
      done
  with Exit -> print_endline "End of service";
	       exit 0;;
	
let main_echoServer () = 
  if Array.length Sys.argv != 2 then
    Printf.eprintf "Usage : echoServer port\n"
  else try
      let port = int_of_string Sys.argv.(1) in
      (*serve socket echo_service*)
      establish_server echo_service port
    with
      Failure("int_of_string") ->
      Printf.eprintf "echoServer : bad port number\n";;

main_echoServer ();;
    
