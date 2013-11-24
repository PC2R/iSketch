SRC		=	Message.java		\
			Client.java		\
			MainWindow.java		\
			Messenger.java		\
			ThreadListener.java	\
			ThreadSender.java	\
			MessagesPanel.java	\
			TextPanel.java

OBJ		=	$(SRC:.java=.class)

DIR		=	./class

NAME		=	Client

%.class	: %.java classP
		javac $< -d $(DIR)

all	: $(OBJ)

$(NAME)	:
	java -classpath $(DIR) $(NAME)

classP	:
	mkdir -p $(DIR)

clean	:
	rm -fr $(DIR)

re	:	clean all
