SRC		=	Client.java \
			MainWindow.java \
			Messenger.java \
			MessagesPanel.java \
			TextPanel.java \
			Test.java

OBJ		=	$(SRC:.java=.class)

DIR		=	./class

NAME		=	Client

%.class	: %.java
		javac $<

all	: $(OBJ)

$(NAME)	: all
	cd $(DIR)
	java $(NAME)

clean	:
	rm -f $(OBJ)

re	:	clean all
