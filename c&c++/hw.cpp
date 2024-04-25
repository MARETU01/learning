#include <iostream>

using namespace std;

int main()
{
    for (int i = 10; i > 0; i--) {
        for (int j = 0; j < i; j++) {
            cout << j + 2;
        }
        cout << endl;
    }
    getchar();
}