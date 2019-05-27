# PasArm (firmware for patient wristband)

To locate the project, go to "PasArm\pca10040\s132\ses" from this location. 

There are two files thet needs to be changed depending on where the project is going to be flashed: 

## Using the nRF52 DK
If a nRF52 Development Kit (DK) is going to be used, you need to make sure these changes are made:

costom_board.h from line 47
//#define PasArm_CUSTOM_BOARD   1
#define PasArm_BOARD_PCA10040 1

BH1790GLC.h from line 39
#define BH1790GLC_MEAS_CONTROL2_VAL   (BH1790GLC_MEAS_CONTROL2_LED_EN_00 | BH1790GLC_MEAS_CONTROL2_LED_ON_TIME_0_3MS | BH1790GLC_MEAS_CONTROL2_LED_CURRENT_10MA) // BOARD_PCA10040
//#define BH1790GLC_MEAS_CONTROL2_VAL   (BH1790GLC_MEAS_CONTROL2_LED_EN_00 | BH1790GLC_MEAS_CONTROL2_LED_ON_TIME_0_3MS | BH1790GLC_MEAS_CONTROL2_LED_CURRENT_20MA) // PasArm_CUSTOM_BOARD

## Flashing program on external device
To program an external microcontroller connected to the DK, use these  modifications:

costom_board.h from line 47
#define PasArm_CUSTOM_BOARD   1
#define PasArm_BOARD_PCA10040 1

BH1790GLC.h from line 39
//#define BH1790GLC_MEAS_CONTROL2_VAL   (BH1790GLC_MEAS_CONTROL2_LED_EN_00 | BH1790GLC_MEAS_CONTROL2_LED_ON_TIME_0_3MS | BH1790GLC_MEAS_CONTROL2_LED_CURRENT_10MA) // BOARD_PCA10040
#define BH1790GLC_MEAS_CONTROL2_VAL   (BH1790GLC_MEAS_CONTROL2_LED_EN_00 | BH1790GLC_MEAS_CONTROL2_LED_ON_TIME_0_3MS | BH1790GLC_MEAS_CONTROL2_LED_CURRENT_20MA) // PasArm_CUSTOM_BOARD

## .hex-file
The .hex file is located in "PasArm\pca10040\s132\ses\Output\Release\Exe"
If you are missing the Output folder you need to build the project first.

## Plotting data recieved from DK
The .py files are for plotting values recieved trough USB from the DK in a graph. Notice: The graph plotting is name sensitive. 
