let max_players = ref 4
let timeout = ref 30
let port = ref 2013
let players = ref ((Hashtbl.create !max_players) : (string, int) Hashtbl.t)
		  
let test command =
  let l = Str.split (Str.regexp "[/]") command in
  match List.nth l 0 with
    "CONNECT" -> let name = String.sub command (String.length "CONNECT" + 1) (String.length command - (String.length "CONNECT" + 1)) in
		 if (Hashtbl.mem !players name) then
		   print_endline (name ^ " is already taken by another player, try another one")
		 else
		   begin
		     print_endline (name ^ " added successfuly to the hashtable");
		     Hashtbl.add !players name 0;
		   end
  | "EXIT" -> let name = String.sub command (String.length "EXIT" + 1) (String.length command - (String.length "EXIT" +1)) in
	      Hashtbl.remove !players name;
	      print_endline (name ^ "removed successfuly from the hashtable");
  | _ -> print_endline "error";;
		     
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
  test "CONNECT/gandalf/";
  test "CONNECT/saroumane/";
  test "CONNECT/avec\\backslash/"; (* avec\backslash *)
  test "CONNECT/avec\\/slash/"; (* avec/slash *)
  test "CONNECT/saroumane/";
  test "EXIT/gandalf/";
  test "EXIT/saroumane/";
  print_endline (string_of_int (Hashtbl.length (!players)));;
  
let () = main
