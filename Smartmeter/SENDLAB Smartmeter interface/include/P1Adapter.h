#pragma once
#include <Arduino.h>

#define P1_MAX_DATAGRAM_SIZE 2048

typedef enum {
    P1_MSG_START,
    P1_MSG_READING,
    P1_MSG_END
} ENUM_P1_MSG_STATE;

class P1Adapter {
    public:
        char p1_buffer[P1_MAX_DATAGRAM_SIZE]; // Complete P1 telegram
        char *p1;

        void init();
        bool capture_p1();
        void p1_reset();
        void p1_store(char ch);

    private:
        ENUM_P1_MSG_STATE p1_msg_state = P1_MSG_START;
};


