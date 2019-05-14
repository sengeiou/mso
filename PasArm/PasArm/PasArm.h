#ifndef PASARM_H__
#define PASARM_H__

#define RED       (0)
#define BLUE      (1)
#define GREEN     (2)

#define counts_befour_masure  (4)   // this is the counts a puls is detected befour duing masurments
#define one_min               (60)  // in secunds
#define PasArm_Hz             (32)  // Hz the code iterates on

#define puls_array            (20)  // 15 the register size for calculating raw data mean
#define bpm_array             (10)  // 10 the register size for calculating the puls mean fore a more reliebal mesurment
#define Reset_to_zero         (0)   // used to set the data back to zero
#define DISCARD               (4)   // discard the puls

struct Optical_puls
{
    uint16_t  adc_data[2];
    uint16_t  puls_data[puls_array];
    uint16_t  puls_mean;
    uint8_t   count;

};

struct BPM_Measurement_Calculation_Values
{
    uint16_t  bpm_mean;
    uint16_t  bpm[bpm_array];
    uint8_t   bpm_array_count;

    uint8_t   puls_count;
    uint8_t   DISCARD_check;

    bool      measure;
    bool      puls_on;
};

void power_management_init(void);
void start_PasArm();

#endif // PASARM_H__