CAMLC = ocamlc
CAMLDOC = ocamldoc

WITHUNIX = unix.cma -cclib -lunix
WITHTHREADS = -thread threads.cma -cclib -lthreads
WITHSTR = str.cma
CUSTOM = -custom

SOURCES = server.ml
INTERFACE = server.mli

EXEC = server
LIBS = $(WITHUNIX) $(WITHSTR) 

all:
	$(CAMLC) -c $(INTERFACE) -o $(EXEC) $(LIBS) $(WITHTHREADS) $(CUSTOM) $(SOURCES)

doc:
	$(CAMLDOC) -v -latex $(INTERFACE) -o doc.tex

clean:
	rm -f $(EXEC) *.cmi *.cmo *~ *.tex
