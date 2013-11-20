(** Documentation for the server side from the iSketch game 
 @author Theo Lebourg
*)

(** Maximum number of players able to play during one game *)
val max_players : int ref

(** This class sets up the server
 @param port listening port number
*)
class server :
int ->
int ->
object
  (** Listening port number of the server *)
  val port_num : int
  val nb_pending : int
  (** TCP/IP socket *)
  val s_descr : Unix.file_descr
  (** Creates a Thread for the game *)
  method init_game : unit -> Thread.t
  (** Waits for potential players connections *)
  method start : unit -> unit
  method start_rounds : unit -> unit
end

