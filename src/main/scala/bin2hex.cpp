#include <iostream>
#include <fstream>
#include <bitset>
#include <string>
#include <iomanip>
using namespace std;

int main(int argc, char* argv[]){
    if(argc != 2){
        cout << "./ben2hex [file.bin]" << endl;
        return -1;
    }
    string filename = string(argv[1]);
    ifstream ifs(filename, ios::binary);
    char buf[4];
    while(true){
        ifs.read(buf, 4);
        if(ifs.eof()) break;
        uint32_t inst = 0;
        inst = uint8_t(buf[0]) | uint(uint8_t((buf[1]))<<8) | uint(uint8_t((buf[2]))<<16) | uint(uint8_t((buf[3]))<<24);
        cout << std::setw(8) << std::setfill('0') << std::hex << inst << endl;
    }
    ifs.close();
}