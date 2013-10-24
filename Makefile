CAMLC = ocamlc
WITHUNIX = unix.cma -cclib -lunix
WITHTHREADS = -thread threads.cma -cclib -lthreads

SOURCES = echoServer.ml
EXEC = echoServer
LIBS = $(WITHUNIX) $(WITHTHREADS)
CUSTOM = -custom

all:
	$(CAMLC) -o $(EXEC) $(LIBS) $(SOURCES)

clean:
	rm -f $(EXEC) *.cmi *.cmo *~
