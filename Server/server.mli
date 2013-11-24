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
  (** Pseudo of the player *)
  val mutable pseudo : string
  (** Unique ID used to define the running order for the game *)
  val mutable number : int
  (** Creates a thread with all the functions needed by the player *)
  method start : unit -> unit
  (** Called when the player stops to play / quit the game *)
  method stop : unit -> unit
  (** Method which adds one player to the game (if its pseudo is not used by another player) *)
  method connection_player : unit -> unit
  (** Simple setter function to set number and pseudo variables *)
  method set_number_and_pseudo : int ref -> string -> unit
  (** Called at the beginning of each round and send roles/word to find to the players *)
  method wait_functions : unit -> unit
  method send_roles_and_word : unit -> unit
  method wait_word_proposition : unit -> unit
  method send_word_proposition : unit -> unit
  method send_word_found : unit -> unit
  method wait_drawing_proposition : unit -> unit
  method send_drawing_proposition : unit -> unit
end

