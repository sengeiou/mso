#include <stdio.h>
#include "boards.h"
#include "app_error.h"

#include "PasArm_timer.h"

#define five_seconds (156)

const nrf_drv_timer_t TIMER_PULS = NRF_DRV_TIMER_INSTANCE(1);

static uint32_t PasArm_timer_count = 0;
static bool PasArm_timer_reset = false;
static bool PasArm_Timer_Event = false;
static bool measuring = false;

uint32_t get_PasArm_timer_count()             { return PasArm_timer_count; }
void set_PasArm_timer_reset(bool timer_reset) { PasArm_timer_reset = timer_reset; }
void set_PasArm_Timer_Event(bool timer_event) { PasArm_Timer_Event = timer_event; }
bool get_PasArm_Timer_Event()                 { return PasArm_Timer_Event; }
void set_measure(bool measure)                { measuring = measure; }

void timer_puls_event_handler(nrf_timer_event_t event_type, void* p_context)
{
    static uint8_t five_s_count = 0;

    switch (event_type)
    {
        case NRF_TIMER_EVENT_COMPARE1:
            if(measuring || five_s_count == five_seconds)
            {
                PasArm_Timer_Event = true;
                five_s_count = 0;
            }
            else
                five_s_count++;

            if(PasArm_timer_reset)
            {
                PasArm_timer_count = 0;
                PasArm_timer_reset = false;
            }
            else
                PasArm_timer_count++;
            break;

        default:
            //Do nothing.
            break;
    }
}

void puls_timer_init()
{
    ret_code_t err_code;

    uint32_t time_ms = 32; //Time(in miliseconds) between consecutive compare events.
    uint32_t time_ticks;
  
    //Configure TIMER_PULS.
    nrf_drv_timer_config_t timer_cfg = NRF_DRV_TIMER_DEFAULT_CONFIG;
    err_code = nrf_drv_timer_init(&TIMER_PULS, &timer_cfg, timer_puls_event_handler);
    APP_ERROR_CHECK(err_code);

    time_ticks = nrf_drv_timer_ms_to_ticks(&TIMER_PULS, time_ms);
    nrf_drv_timer_extended_compare(
         &TIMER_PULS, NRF_TIMER_CC_CHANNEL1, time_ticks, NRF_TIMER_SHORT_COMPARE1_CLEAR_MASK, true);

    nrf_drv_timer_enable(&TIMER_PULS);
}