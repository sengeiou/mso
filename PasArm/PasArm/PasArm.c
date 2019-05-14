#include <stdio.h>
#include <math.h>
#include "boards.h"
#include "app_util_platform.h"
#include "app_error.h"
#include "nrf_pwr_mgmt.h"

#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

#include "PasArm_timer.h"
#include "BH1790GLC.h"
#include "BLE.h"
#include "MPU6050.h"
#include "fall_detection.h"
#include "PasArm.h"

struct Optical_puls Optical_puls_Init()
{ 
    struct Optical_puls opi = {
          .adc_data         = {0},
          .puls_data        = {0},
          .puls_mean        = 0,

          .count            = 0,
          };
    return opi;
};

struct BPM_Measurement_Calculation_Values BPM_MCV_Init()
{ 
    struct BPM_Measurement_Calculation_Values BMP_MCV = {
          .bpm_mean         = 0,
          .bpm              = {0},
          .bpm_array_count  = 0,

          .puls_count       = 0,
          .DISCARD_check    = 0,

          .measure          = false,
          .puls_on          = false
          };
    return BMP_MCV;
};

/**@brief Function for initializing power management.
 */
void power_management_init(void)
{
    ret_code_t err_code;
    err_code = nrf_pwr_mgmt_init();
    APP_ERROR_CHECK(err_code);
}

/**@brief Function for handling the idle state (main loop).
 *
 * @details If there is no pending log operation, then sleep until next the next event occurs.
 */
static void idle_state_handle(void)
{
    UNUSED_RETURN_VALUE(NRF_LOG_PROCESS());
    nrf_pwr_mgmt_run();
}

uint16_t mean_val(uint16_t MEAN, uint16_t* data, uint16_t new_data, uint8_t count, const uint8_t size_array)
{
    uint32_t mean_ = 0;

    if(count != size_array)
    {
        data[count] = new_data;

        if(count == 0)
        {   
            if(MEAN != 0)
                mean_ = (MEAN + data[count]) / 2;
            else 
                mean_ = data[count];

            MEAN = mean_;
        }
        else
        {    
            for(uint8_t i = 0; i <= count; i++)
            {
                if(i == count)
                    MEAN = mean_ / count;
                else
                    mean_ += data[i];
            }  
        }
        count++;
    }
    else
    {
        for(uint8_t i = 0; i <= count; i++)
        {
            if(i == count)
                MEAN = mean_ / count;
            else
            {
                if(i == count -1)
                    data[i] = new_data;
                else
                    data[i] = data[i+1];

                mean_ += data[i];
            }
        }
    }
    return MEAN;
}

void BMP_masure_Idle(struct Optical_puls *bh, struct BPM_Measurement_Calculation_Values *bmp_mcv)
{
    B_L_E_send("Plase your hand on the sensor", NULL);

    if(bsp_board_led_state_get(RED))
        bsp_board_led_invert(RED);

    bmp_mcv->puls_count = Reset_to_zero;
    bh->puls_mean = bmp_mcv->bpm_mean = Reset_to_zero;
    bh->count = bmp_mcv->bpm_array_count = Reset_to_zero;
    bmp_mcv->measure = bmp_mcv->puls_on = false;
}

void BMP_masure_Active(struct Optical_puls *bh, struct BPM_Measurement_Calculation_Values *bmp_mcv)
{
    bmp_mcv->measure = true;

    bh->puls_mean = mean_val(bh->puls_mean, bh->puls_data, bh->adc_data[1], bh->count, puls_array);
    if(bh->count != puls_array)
        bh->count++;
    else
    {       
        if(bh->puls_mean < bh->adc_data[1]-10 && !bmp_mcv->puls_on && bmp_mcv->DISCARD_check > DISCARD)
        {
            bmp_mcv->puls_on = true;
            if(get_active_leds())
                bsp_board_led_invert(RED);

            bmp_mcv->puls_count++;

            if(bmp_mcv->puls_count == counts_befour_masure)
            {
                bmp_mcv->bpm_mean = mean_val(bmp_mcv->bpm_mean, bmp_mcv->bpm, (counts_befour_masure * one_min * 1000) / (get_PasArm_timer_count() * PasArm_Hz), bmp_mcv->bpm_array_count, bpm_array);
                bmp_mcv->puls_count = bmp_mcv->DISCARD_check = Reset_to_zero;
                set_PasArm_timer_reset(true);

                if(bmp_mcv->bpm_array_count < bpm_array)
                    bmp_mcv->bpm_array_count++;

                B_L_E_send("BPM: ", bmp_mcv->bpm_mean);

                if(160 < bmp_mcv->bpm_mean || bmp_mcv->bpm_mean < 30)
                    B_L_E_Alarm(2);
            }
        }
        else if(bh->puls_mean > bh->adc_data[1]+10 && bmp_mcv->puls_on && bmp_mcv->DISCARD_check > DISCARD)
        {
            bmp_mcv->puls_on = false;
            bmp_mcv->DISCARD_check = Reset_to_zero;
            if(get_active_leds())
                bsp_board_led_invert(RED);

        }
        bmp_mcv->DISCARD_check++;
    }
}

void start_PasArm()
{
    ret_code_t err_code;

    struct Optical_puls                       bh      = Optical_puls_Init();
    struct Accelerometer                      acc     = Acc_Init();
    struct BPM_Measurement_Calculation_Values bmp_mcv = BPM_MCV_Init();

    while (true)
    {
        idle_state_handle();

        if(!bmp_mcv.measure)
            set_PasArm_timer_reset(true);

        if(connected() && get_PasArm_Timer_Event())
        {
            set_PasArm_Timer_Event(false);

            BH1790GLC_adc_rx(bh.adc_data);
            MPU_6050_acc_rx(acc.acc_data);

            if(bh.adc_data[0] > 350 || bh.adc_data[1] < 1000 )
                BMP_masure_Idle(&bh, &bmp_mcv);
            else
                BMP_masure_Active(&bh, &bmp_mcv);

            set_measure(bmp_mcv.measure);

            if(fall_detection(&acc))
                B_L_E_send("FALL", NULL);
        }
        NRF_LOG_FLUSH();
    }
}