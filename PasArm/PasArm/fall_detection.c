#include <stdio.h>
#include <math.h>

#include "nrf_log.h"
#include "nrf_log_ctrl.h"

#include "fall_detection.h"
#include "MPU6050.h"
#include "BLE.h"


struct Accelerometer Acc_Init()
{
    struct Accelerometer ini = {
           .acc_data   = {0},
           .x          = {0},
           .y          = {0},
           .z          = {0},
           .xdiff      = 0,
           .ydiff      = 0,
           .zdiff      = 0,
           .tott_diff  = 0,
           .detection  = {0},
           .firts_entry = true };
    return ini;
}

void motion_update(struct Accelerometer *acc)
{
    acc->x[0] = abs(acc->acc_data[x_data]);
    acc->y[0] = abs(acc->acc_data[y_data]);
    acc->z[0] = abs(acc->acc_data[z_data]);

    acc->xdiff = acc->x[0] - acc->x[1];
    acc->ydiff = acc->y[0] - acc->y[1];
    acc->zdiff = acc->z[0] - acc->z[1];

    acc->tott_diff = acc->xdiff + acc->ydiff + acc->zdiff;
    
    acc->x[1] = acc->x[0];
    acc->y[1] = acc->y[0];
    acc->z[1] = acc->z[0];
}

bool fall_detection(struct Accelerometer *acc)
{
    static uint8_t count  = 0;
    int16_t distance      = 0;
    bool fall = false;

    motion_update(acc);

    if(!acc->firts_entry)
    {
        if(count != fall_array_size)
        {
            acc->detection[count] = acc->tott_diff;
            count++;
        }
        else
        {
            for(uint8_t i = 0; i < count; i++)
            {
                if(i < count-1)
                    acc->detection[i] = acc->detection[i+1];
                else
                    acc->detection[i] = acc->tott_diff;

                distance += acc->detection[i]/(32*16);

                if(i == 0 && acc->detection[i] > -acc_fall_value && !fall)
                    fall  = true;
                else if(i > shake && fall && (acc->detection[i] > acc_fall_value || acc->detection[i] < -acc_fall_value))
                    fall = false;
            }
        }
    }
    else
        acc->firts_entry = false;

    if(Sent_Acc_Data())
        B_L_E_send("FALL: ", acc->tott_diff);

    /*
    NRF_LOG_INFO("x=%i", acc->x[0]);
    NRF_LOG_INFO("y=%i", acc->y[0]);
    NRF_LOG_INFO("z=%i", acc->z[0]);*/
    NRF_LOG_INFO("diff=%i", acc->tott_diff);
    //NRF_LOG_INFO("distance =%i", distance);
    //NRF_LOG_FLUSH();

    return fall;
}