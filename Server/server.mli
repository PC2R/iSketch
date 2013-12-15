(** Server-side from the iSketch game *)

(** Maximum players capacity *)
val max_players : int ref

(** Timeout when a word is found *)
val timeout : int ref

(** Server port number *)
val port : int ref

(** Number of players needed to report a cheating behavior *)
val cheat_parameter : int ref

(** Verbose mode enabled or not *)
val verbose_mode : bool ref

(** Dictionary filename *)
val dictionary_filename : string ref

(** List of words from the dictionary *)
val dictionary_words : string list ref

(** Log file *)
val logfile : string

(** Single file which stands for database *)
val registered_players : string

(** Word to find at each round *)
val word : string ref

(** Says whether or not the word has been found *)
val word_found : bool ref

(** Backs up the number of people who have found the word at each round *)
val word_finders : int ref

(** At each round, each player is either a « drawer » or a « finder » *)
type role = DRAWER | FINDER

(** List of all the players *)
val players :
  < get_name : unit -> string; get_role : unit -> role;
    get_score_game : unit -> int; get_score_round : unit -> int;
    get_status : unit -> bool; send_command : string -> unit;
    set_role : role -> unit; set_score_round : int -> unit;
    start_game : unit -> unit; update_score_game : unit >
  list ref

(** Mutex to prevent multiple players to connect/register/login simultaneously *)
val mutex_players : Mutex.t


val mutex_maximum_players : Mutex.t
val condition_players : Condition.t

val condition_end_round : Condition.t
val mutex_end_round : Mutex.t

val mutex_guessed_word : Mutex.t

val players_connected : int ref
val round : int ref
val rgb : string ref
val size : string ref
val line : string ref
val score_round_finder : int ref
val score_round_drawer : int ref
val timeout_on : bool ref
val round_canceled : bool ref
val cheat_counter : int ref
val thread_timeout : Thread.t ref
val trace : string -> unit
val notify_timeout : unit -> unit
val update_variables : unit -> unit
val init_variables : unit -> unit
val remove : 'a -> 'a list -> 'a list
val read_file : in_channel -> string list
val init_dict : unit -> string list
val unescaped : string -> string
val escaped : string -> string
val exists_in_db : string -> bool
val is_ok : string -> string -> bool
val gen_salt : int -> string
val exists : string -> bool
val generate_name : string -> string
val register_in_db : string -> string -> unit
val update_statistics : unit -> unit
val choose_word : unit -> unit
val my_input_line : Unix.file_descr -> string
val my_nth : string -> int -> string
val notify_players : string -> string -> unit
val notify_exit : string -> unit
val notify_guess : string -> string -> unit
val notify_word_found : string -> unit
val notify_line : string -> unit
val notify_cheat : string -> unit
val notify_talk : string -> string -> unit
val send_connected_command : unit -> unit
val send_new_round_command : string -> unit
val choose_drawer : unit -> int
val send_score_round_command : unit -> unit
val send_end_round_command : unit -> unit
val reset_score_players : unit -> unit
class player :
  string ->
  Unix.file_descr ->
  object
    val mutable connected : bool
    val mutable name : string
    val mutable role : role
    val s_descr : Unix.file_descr
    val mutable score_game : int
    val mutable score_round : int
    method get_name : unit -> string
    method get_role : unit -> role
    method get_score_game : unit -> int
    method get_score_round : unit -> int
    method get_status : unit -> bool
    method send_command : string -> unit
    method set_role : role -> unit
    method set_score_round : int -> unit
    method start_game : unit -> unit
    method update_score_game : unit
  end
val welcome_player : string -> Unix.file_descr -> unit
val connection_player : Unix.file_descr * 'a -> unit
class server :
  int ->
  int ->
  object
    val nb_pending : int
    val port_num : int
    val s_descr : Unix.file_descr
    method start_game : unit -> unit
    method wait_connections : unit -> unit
  end
val read_db : in_channel -> string
val generate_response : string -> string
val response : Unix.file_descr * 'a -> unit
class serverHTTP :
  int ->
  int ->
  object
    val nb_pending : int
    val port_num : int
    val s_descr : Unix.file_descr
    method start : unit -> unit
  end
val main : unit -> unit
