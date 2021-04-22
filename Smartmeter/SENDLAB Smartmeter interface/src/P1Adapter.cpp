#include <P1Adapter.h>

void P1Adapter::init()
{
    Serial.flush();
    Serial.begin(115200, SERIAL_8N1);
    delay(2000);
    Serial.swap();
}

bool P1Adapter::capture_p1()
{
    bool retval = false;
    if (Serial.available())
    {        
        while(Serial.available())
        {
            char ch = Serial.read();

            switch(p1_msg_state){
                case P1_MSG_START:
                    if(ch == '/'){
                        p1_msg_state = P1_MSG_READING;

                        p1_reset();
                        p1_store(ch);
                    }

                break;
                case P1_MSG_READING:
                    p1_store(ch);
                    if(ch == '!'){
                        p1_msg_state = P1_MSG_END;

                    }
                break;
                case P1_MSG_END:
                    p1_store(ch);
                    if(ch == '\n'){
                        p1_store('\0');
                        p1_msg_state = P1_MSG_START;
                        retval = true;
                    }
                break;
                default:
                    retval = false;
                    break;
            }            
        }
    }

    return retval;
}

void P1Adapter::p1_store(char ch){
    if ((p1 - p1_buffer) < P1_MAX_DATAGRAM_SIZE)
    {
        *p1 = ch;
        p1++;
    }
}

void P1Adapter::p1_reset(){
    p1 = p1_buffer;
    *p1 = '\0';
}