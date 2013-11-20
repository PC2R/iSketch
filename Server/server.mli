(** The server side from the iSketch game *)

(** Maximum number of players able to play during one game *)
val max_players : int ref

(** This class sets up the server *)
class server :
int ->
int ->
object
  (** Listening port number of the server *)
  val port_num : int
  val nb_pending : int
  (** TCP/IP socket *)
  val s_descr : Unix.file_descr

  method init_game : unit -> Thread.t
  method start : unit -> unit
  method start_rounds : unit -> unit
  method treat : Unix.file_descr -> Unix.sockaddr -> unit
end
