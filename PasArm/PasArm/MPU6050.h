#ifndef MPU6050_H__
#define MPU6050_H__

/* MPU-6050 I2C-ADDRESS */
#define GRD_ADDR  (0x68)  //AD0 => GRN
#define VDD_ADDR  (0x69)  //AD0 => VDD

#define MPU_6050_TWI_ADDR VDD_ADDR

/* MPU-6050 Register Map */
#define WHO_AM_I            (0x75)
#define ACCEL_CONFIG        (0x1C)
#define ACCEL_XOUT_H        (0x3B)
#define TEMP_OUT_H          (0x41)
#define GYRO_CONFIG         (0x1B)
#define GYRO_XOUT_H         (0x43)
#define PWR_MGMT_1          (0x6B)
#define PWR_MGMT_2          (0x6C)

#define PWR_MGMT_2_40_HZ    (0xC0)
#define PWR_MGMT_2_20_HZ    (0x80)
#define PWR_MGMT_2_5_HZ     (0x40)
#define PWR_MGMT_2_1_25_HZ  (0x00)
#define PWR_MGMT_2_ACC_X_D  (0x20)  // _D => DISABLED
#define PWR_MGMT_2_ACC_Y_D  (0x10)
#define PWR_MGMT_2_ACC_Z_D  (0x08)
#define PWR_MGMT_2_GY_X_D   (0x04)
#define PWR_MGMT_2_GY_Y_D   (0x02)
#define PWR_MGMT_2_GY_Z_D   (0x01)

#define PWR_MGMT_1_SET_UP   (0x28)  // puts the device inn sleep cycle, disabels temp and uses internal 8MHz clk.
#define PWR_MGMT_2_SET_UP   (PWR_MGMT_2_40_HZ | PWR_MGMT_2_GY_X_D | PWR_MGMT_2_GY_Y_D | PWR_MGMT_2_GY_Z_D)  // only Accel active, wake-up 40Hz cycle

#define x_data              0
#define y_data              1
#define z_data              2

bool MPU6050_init(void);
void MPU6050_SLEEP(void);
void MPU_6050_acc_rx(uint16_t *data);

#endif // MPU6050_H__