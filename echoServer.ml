(* Echo Server *)
(* compilation : $ ocamlc -o echoServer unix.cma echoServer.ml *)
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
    let (s, caller) = Unix.accept s_descr in
    match Unix.fork() with
      (* Unix.fork() returns 0 to the child process *)
      0 -> if Unix.fork () <> 0 then exit 0;
	   let inchan = Unix.in_channel_of_descr s
	   and outchan = Unix.out_channel_of_descr s in
	   service inchan outchan;
	   close_in inchan;
	   close_out outchan;
	   exit 0;
      (* parent *)
      | pid -> Unix.close s;
	       ignore (Unix.waitpid [] pid);
  done;;

let echo_service in_channel out_channel =
  try while (true) do
	let line = input_line in_channel in
	(* \013 = enter to exit the client *)
	if (line = "") or (line = "\013") then
	  raise Exit
	else
	  output_string out_channel (line^"\n");
	flush out_channel
      done
  with Exit -> flush stdout;
	       Printf.printf "End of service\n";
	       exit 0;;
			     
let main () = 
  if Array.length Sys.argv != 2 then
    Printf.eprintf "Usage : echoServer port\n"
  else try
      let port = int_of_string Sys.argv.(1) in
      (*serve socket echo_service*)
      establish_server echo_service port
    with
      Failure("int_of_string") ->
      Printf.eprintf "echoServer : bad port number\n";;

main ();;
    
