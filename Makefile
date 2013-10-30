CAMLC = ocamlc
WITHUNIX = unix.cma -cclib -lunix
WITHTHREADS = -thread threads.cma -cclib -lthreads
WITHSTR = str.cma

SOURCES = serveur.ml
EXEC = serveur
LIBS = $(WITHUNIX) $(WITHSTR) 

all:
	$(CAMLC) -o $(EXEC) $(LIBS) $(SOURCES)

clean:
	rm -f $(EXEC) *.cmi *.cmo *~
