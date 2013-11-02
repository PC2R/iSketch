SRC=	Client.java \
	MainWindow.java \
	Test.java
OBJ=	Client.class \
	MainWindow.class \
	Test.class
NAME=	Client
TEST=	Test

all:
	javac $(SRC)

$(NAME): all
	java $(NAME)

$(TEST): all
	java $(TEST)

clean:
	rm -f $(OBJ)

fclean: clean
	rm -f $(NAME)

re: fclean all 
