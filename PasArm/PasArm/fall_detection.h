#ifndef FALL_DETECTION_H__
#define FALL_DETECTION_H__

#define fall_array_size   (64)
#define shake             (16)
#define falling           (32)

#define acc_fall_value    (25000)

struct Accelerometer
{
    int16_t acc_data[3];
    int16_t x[2], y[2], z[2];
    int16_t xdiff, ydiff, zdiff;
    int32_t tott_diff;
    int32_t detection[fall_array_size];

    bool firts_entry;
};

struct Accelerometer Acc_Init();

bool fall_detection(struct Accelerometer *acc);

#endif // FALL_DETECTION_H__