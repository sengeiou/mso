#include <stdio.h>
#include "boards.h"
#include "nrf_drv_twi.h"
#include "nrf_delay.h"

#include "nrf_log.h"
#include "nrf_log_ctrl.h"
#include "nrf_log_default_backends.h"

#include "BH1790GLC.h"
#include "MPU6050.h"

#define RTA_SCL_PIN 26
#define RTA_SDA_PIN 25

/* TWI instance ID. */
#if TWI0_ENABLED
#define TWI_INSTANCE_ID     0
#elif TWI1_ENABLED
#define TWI_INSTANCE_ID     1
#endif

/* TWI instance. */
static const nrf_drv_twi_t m_twi_PasArm = NRF_DRV_TWI_INSTANCE(TWI_INSTANCE_ID);

/**
 * @brief TWI initialization.
 */
void twi_init (void)
{
    ret_code_t err_code;

    const nrf_drv_twi_config_t twi_config = {
       .scl                = RTA_SCL_PIN,
       .sda                = RTA_SDA_PIN,
       .frequency          = NRF_DRV_TWI_FREQ_100K,
       .interrupt_priority = APP_IRQ_PRIORITY_HIGH,
       .clear_bus_init     = false
    };

    err_code = nrf_drv_twi_init(&m_twi_PasArm, &twi_config, NULL, NULL);
    APP_ERROR_CHECK(err_code);

    nrf_drv_twi_enable(&m_twi_PasArm);
}

bool BH1790GLC_init(void)
{
    uint8_t reg[2]        = {BH1790GLC_PART_ID, BH1790GLC_MANUFACTURER_ID};
    uint8_t MEAS_setUP[4] = {BH1790GLC_MEAS_CONTROL1, BH1790GLC_MEAS_CONTROL1_VAL, BH1790GLC_MEAS_CONTROL2_VAL, BH1790GLC_MEAS_START_VAL};
    uint8_t data[3]       = {0};

    NRF_LOG_INFO("Starting BH1790GLC_init");
    NRF_LOG_FLUSH();


    nrf_drv_twi_tx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, &reg[0], sizeof(reg[0]), false);
    nrf_drv_twi_rx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, &data[0], sizeof(data[0]));
    if (data[0] != BH1790GLC_PART_ID_VAL)
        return false;

    NRF_LOG_INFO("BH1790GLC_PART_ID_VAL is corect");
    NRF_LOG_FLUSH();

    nrf_drv_twi_tx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, &reg[1], sizeof(reg[1]), false);
    nrf_drv_twi_rx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, &data[0], sizeof(data[0]));
    if (data[0] != BH1790GLC_MANUFACTURER_ID_VAL)
        return false;

    NRF_LOG_INFO("BH1790GLC_MANUFACTURER_ID_VAL is corect");
    NRF_LOG_FLUSH();

    nrf_drv_twi_tx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, MEAS_setUP, sizeof(MEAS_setUP), false);
    nrf_drv_twi_rx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, data, sizeof(data));
    if (data[0] != BH1790GLC_MEAS_CONTROL1_VAL && data[1] != BH1790GLC_MEAS_CONTROL2_VAL && data[2] != BH1790GLC_MEAS_START_VAL)
        return false;

    NRF_LOG_INFO("BH1790GLC_MEAS setup is compleated");
    NRF_LOG_FLUSH();
    
    return true;
}

void BH1790GLC_adc_rx(uint16_t* MEAS_VAL)
{
    uint8_t MEAS_addres = 0x54;
    uint8_t data[4] = {0};

    nrf_drv_twi_tx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, &MEAS_addres, sizeof(4), false);
    nrf_drv_twi_rx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, data, sizeof(data));

    MEAS_VAL[0] = (data[1]<<8)|data[0];
    MEAS_VAL[1] = (data[3]<<8)|data[2];
}

void BH1790GLC_SLEEP(void)
{
    uint8_t sleep[2] = {BH1790GLC_RESET, 0x80};

    nrf_drv_twi_tx(&m_twi_PasArm, BH1790GLC_DEVICE_ADDRESS, sleep, sizeof(sleep), false);
}

/**
 * @Initing MPU6050 and returns conection status
 */
bool MPU6050_init(void)
{   
    ret_code_t err_code;

    uint8_t reset[2]        = {PWR_MGMT_1, 0x80};
    uint8_t init_1_data[2]  = {PWR_MGMT_1, PWR_MGMT_1_SET_UP/*, PWR_MGMT_2_SET_UP*/};
    uint8_t init_2_data[2]  = {PWR_MGMT_2, PWR_MGMT_2_SET_UP/*, PWR_MGMT_2_SET_UP*/};

    // Write zero to the PWR_MGMT_1 register to wake up the MPU-6050
    nrf_drv_twi_tx(&m_twi_PasArm, MPU_6050_TWI_ADDR, reset, sizeof(reset), false);
    nrf_delay_ms(100);

    err_code = nrf_drv_twi_tx(&m_twi_PasArm, MPU_6050_TWI_ADDR, init_1_data, sizeof(init_1_data), false);
    if(err_code != NRF_SUCCESS)
        return false;

    err_code = nrf_drv_twi_tx(&m_twi_PasArm, MPU_6050_TWI_ADDR, init_2_data, sizeof(init_2_data), false);
    if(err_code != NRF_SUCCESS)
        return false;

    return true;
}

void MPU6050_SLEEP(void)
{
    ret_code_t err_code;

    uint8_t sleep[2] = {PWR_MGMT_1, 0x40};
    err_code = nrf_drv_twi_tx(&m_twi_PasArm, MPU_6050_TWI_ADDR, sleep, sizeof(sleep), false);
}

/**
 * @geting raw data from MPU_6050 acc.
 */
void MPU_6050_acc_rx(uint16_t *data)
{
    uint8_t acc_zero = ACCEL_XOUT_H;
    uint8_t acc_data[6] = {0};

    nrf_drv_twi_tx(&m_twi_PasArm, MPU_6050_TWI_ADDR, &acc_zero, sizeof(acc_zero), false);
    nrf_drv_twi_rx(&m_twi_PasArm, MPU_6050_TWI_ADDR, acc_data, sizeof(acc_data));

    data[x_data] = (acc_data[0]<<8)|acc_data[1];
    data[y_data] = (acc_data[2]<<8)|acc_data[3];
    data[z_data] = (acc_data[4]<<8)|acc_data[5];
}
