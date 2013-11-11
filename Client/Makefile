SRC		=	Client.java			\
			MainWindow.java		\
			Test.java

OBJ		=	$(SRC:.java=.class)

%.class	: %.java
		javac $<

all		: $(OBJ)

clean	:
	rm -f $(OBJ)

re		:	clean all
