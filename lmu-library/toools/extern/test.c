#include<stdio.h>

int main(int argc, char** argv)
{
	if (argc == 1)
	{
		printf("Exactly one numerical argument is required\n");
	}
	else
	{
		int n = atoi(argv[1]);
		printf("%d\n", n + 1);
	}

	return 0;
}
