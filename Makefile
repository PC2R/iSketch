CAMLC = ocamlc
WITHUNIX = unix.cma -cclib -lunix
WITHTHREADS = -thread threads.cma -cclib -lthreads

SOURCES = serveur.ml
EXEC = serveur
LIBS = $(WITHUNIX)

all:
	$(CAMLC) -o $(EXEC) $(LIBS) $(SOURCES)

clean:
	rm -f $(EXEC) *.cmi *.cmo *~
