CAMLC = ocamlc
CAMLDOC = ocamldoc

WITHUNIX = unix.cma -cclib -lunix
WITHTHREADS = -thread threads.cma -cclib -lthreads
WITHSTR = str.cma
CUSTOM = -custom

SOURCES = server.ml
INTERFACE = server.mli

EXEC = server
LIBS = $(WITHUNIX) $(WITHSTR) $(WITHTHREADS)

all:
	$(CAMLC) $(LIBS) -c $(INTERFACE)
	$(CAMLC) -o $(EXEC) $(LIBS) $(WITHTHREADS) $(SOURCES)
	$(CAMLDOC) -I +threads -v -latex $(INTERFACE) -o doc/doc.tex

clean:
	rm -f $(EXEC) *.cm[io] *~
