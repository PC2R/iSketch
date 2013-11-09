let max_players = ref 4
let number_connections = let c = ref 0 in (fun () -> incr c; !c);;
let players_connected = ref 0
let timeout = ref 30
let port = ref 2013
let players = ref ((Hashtbl.create !max_players) : (string, int) Hashtbl.t)
let verbose_mode = ref false

let my_input_line file_descr =
  let s = " " and r = ref "" in
  while (Unix.read file_descr s 0 1 > 0) && s.[0] <> '\n' do
    r := !r ^ s
  done;
  !r;;

exception Fin;;

class connexion sd (sa : Unix.sockaddr) waiting_player =
	object (self)
	  
	  val s_descr = sd
	  val s_addr = sa
	  val mutable number = 0
	  val mutable is_waiting = false
	  
	  initializer
	    number <- number_connections();
	    if (!verbose_mode) then
	      print_endline ("Connexion number " ^ string_of_int number ^ " has been created.");
	    is_waiting <- waiting_player;
	    if not is_waiting then
	      incr players_connected
	      
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
	    Unix.close s_descr;
	    decr (players_connected);
	    if (!verbose_mode) then
	      print_endline ("Connexion number " ^ string_of_int number ^ " has been lost.");
	  
	  method run () =
	    try
	      while true do
		let command = my_input_line s_descr in
		let l = Str.split (Str.regexp "[/]") command in
		match List.nth l 0 with
		  "CONNECT" -> let name = String.sub command 8 (String.length command - 8) in
			       if (Hashtbl.mem !players name) then
				 let result = name ^ " is already taken by a player, try another one\n" in
				 ignore (Unix.write s_descr result 0 (String.length result));
			       else
				 begin
				   let result = "CONNECTED/" ^ name ^ "\n" in
				   ignore (Unix.write s_descr result 0 (String.length result));
				   Hashtbl.add !players name 0
				 end;
		 | "EXIT" -> let name = String.sub command 5 (String.length command - 5) in
			       if (Hashtbl.mem !players name) then
				 begin
				   let result = "EXITED/" ^ name ^ "\n" in
				   ignore (Unix.write s_descr result 0 (String.length result));
				   Hashtbl.remove !players name
				 end
			       else
				 let result = name ^ "is not in the hashtable, it cannot be removed\n" in
				 ignore (Unix.write s_descr result 0 (String.length result));
		 | _ -> let result = command ^ " is unknown (try CONNECT/user/ or EXIT/user/)\n" in
			ignore (Unix.write s_descr result 0 (String.length result));
	      done
	    with
	      exn -> print_string (Printexc.to_string exn) ; print_newline()
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
		print_endline ("Verbose mode activated");
		print_endline ("Server successfuly started with parameters : " ^ string_of_int !max_players ^ " maximum players and " ^ string_of_int !timeout ^ " seconds of timeout.");
		print_endline ("Server is waiting for players connections on port " ^ string_of_int port ^ ".");
	      end;
	    while true do
	      let (service_sock, client_sock_addr) =
		Unix.accept s_descr
	      in
	      if (!players_connected != !max_players) then
		s#treat service_sock client_sock_addr false
	      else
		s#treat service_sock client_sock_addr true
	    done
	  method treat service_sock client_sock_addr waiting_player =
	    ignore ((new connexion service_sock client_sock_addr waiting_player)#start())
	end;;

let main () =
  begin
    let speclist =
      [("-max", Arg.Set_int max_players, " Sets maximum number of players (default : 4)");
       ("-timeout", Arg.Set_int timeout, " Sets the length of the timeout (in seconds) when the word is found (default : 30)");
       ("-port", Arg.Set_int port, " Sets the listening port for the server (default : 2013)");
       ("-v", Arg.Set verbose_mode, " Enables verbose mode");
      ]
    in let usage_msg = "Options availables for iSketch server :"
       in Arg.parse speclist print_endline usage_msg;
  end;
  (new server !port 2)#start();;

main ();;
