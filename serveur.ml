let max_players = ref 4
let timeout = ref 3
let port = ref 2013

let main =
  begin
    let speclist =
      [("-max", Arg.Set_int max_players, "Sets maximum number of players");
       ("-timeout", Arg.Set_int timeout, "Sets the length of the timeout when the word is found");
       ("-port", Arg.Set_int port, "Sets the listening port for the server");]
    in let usage_msg = "Options availables for iSketch server :"
       in Arg.parse speclist print_endline usage_msg;
  end

let () = main
