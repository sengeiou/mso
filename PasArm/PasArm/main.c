#include <stdio.h>
#include "boards.h"
#include "app_util_platform.h"
#include "app_error.h"

#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

#include "PasArm_timer.h"
#include "BH1790GLC.h"
#include "BLE.h"
#include "MPU6050.h"
#include "PasArm.h"


/**
 * @brief Function for main application entry.
 */

int main(void)
{
    APP_ERROR_CHECK(NRF_LOG_INIT(NULL));
    NRF_LOG_DEFAULT_BACKENDS_INIT();
    power_management_init();

    if(!B_L_E_init())
        bsp_board_led_on(RED);

    puls_timer_init();

    twi_init();
    if(!BH1790GLC_init())
    {
        NRF_LOG_INFO("Did not conect to BH1790GLC");
        NRF_LOG_INFO("Exiting!");
        NRF_LOG_FLUSH();

        bsp_board_led_on(RED);
    }
    else
    {
        NRF_LOG_INFO("Conected to BH1790GLC");
        NRF_LOG_INFO("Ready to do masurment.");
        NRF_LOG_FLUSH();
    }
    if(!MPU6050_init())
        bsp_board_led_on(RED);

    start_PasArm();
}