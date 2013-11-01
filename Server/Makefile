CAMLC = ocamlc
WITHUNIX = unix.cma -cclib -lunix
WITHTHREADS = -thread threads.cma -cclib -lthreads
WITHSTR = str.cma
CUSTOM = -custom

SOURCES = serveur.ml
EXEC = serveur
LIBS = $(WITHUNIX) $(WITHSTR) 

all:
	$(CAMLC) -o $(EXEC) $(LIBS) $(WITHTHREADS) $(CUSTOM) $(SOURCES) 

clean:
	rm -f $(EXEC) *.cmi *.cmo *~
