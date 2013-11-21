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

(** This class sets up one player
@param sd socket descriptor
@param sa socket address
 *)
class player :
Unix.file_descr ->
Unix.sockaddr ->
object
  val s_descr : Unix.file_descr
  val s_addr : Unix.sockaddr
  val mutable pseudo : string
  val mutable number : int
  method start : unit -> unit
  method stop : unit -> unit
  method connection_player : unit -> unit
  method set_number_and_pseudo : int ref -> string -> unit
  method send_roles_and_word : unit -> unit
  method wait_word_proposition : unit -> unit
  method send_word_proposition : unit -> unit
  method wait_drawing_proposition : unit -> unit
  method send_drawing_proposition : unit -> unit
end
