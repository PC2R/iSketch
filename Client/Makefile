SRC		=	Message.java		\
			Client.java		\
			MainWindow.java		\
			Messenger.java		\
			ThreadListener.java	\
			ThreadSender.java	\
			MessagesPanel.java	\
			TextPanel.java		\
			DrawMenu.java		\
			DrawPanel.java		\
			DrawPoint.java		\
			ColorActionListener.java\
			SizeActionListener.java	\
			BoardPanel.java		\
			Player.java		\
			ChatPanel.java		\
			OptionActionListener.java

OBJ		=	$(addprefix $(DIR)/, $(SRC:.java=.class))

DIR		=	./class


$(addprefix $(DIR)/, %.class) :	%.java
	javac $< -d $(DIR)

all	: $(DIR)  $(OBJ)

$(DIR)	:
	mkdir $(DIR)

clean	:
	rm -fr $(DIR)

re	:	clean all
