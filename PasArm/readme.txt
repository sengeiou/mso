to find the project go to PasArm\pca10040\s132\ses from hear.

there are to files thet needs to be changed depending on whar is going to be programed.
if the nrf52832 dk is going to be programed you need to make sure this canges are made:

costom_board.h from line 47
//#define PasArm_CUSTOM_BOARD   1
#define PasArm_BOARD_PCA10040 1

BH1790GLC.h from line 39
#define BH1790GLC_MEAS_CONTROL2_VAL   (BH1790GLC_MEAS_CONTROL2_LED_EN_00 | BH1790GLC_MEAS_CONTROL2_LED_ON_TIME_0_3MS | BH1790GLC_MEAS_CONTROL2_LED_CURRENT_10MA) // BOARD_PCA10040
//#define BH1790GLC_MEAS_CONTROL2_VAL   (BH1790GLC_MEAS_CONTROL2_LED_EN_00 | BH1790GLC_MEAS_CONTROL2_LED_ON_TIME_0_3MS | BH1790GLC_MEAS_CONTROL2_LED_CURRENT_20MA) // PasArm_CUSTOM_BOARD

for the costom board the changes are:

costom_board.h from line 47
#define PasArm_CUSTOM_BOARD   1
#define PasArm_BOARD_PCA10040 1

BH1790GLC.h from line 39
//#define BH1790GLC_MEAS_CONTROL2_VAL   (BH1790GLC_MEAS_CONTROL2_LED_EN_00 | BH1790GLC_MEAS_CONTROL2_LED_ON_TIME_0_3MS | BH1790GLC_MEAS_CONTROL2_LED_CURRENT_10MA) // BOARD_PCA10040
#define BH1790GLC_MEAS_CONTROL2_VAL   (BH1790GLC_MEAS_CONTROL2_LED_EN_00 | BH1790GLC_MEAS_CONTROL2_LED_ON_TIME_0_3MS | BH1790GLC_MEAS_CONTROL2_LED_CURRENT_20MA) // PasArm_CUSTOM_BOARD


the .hex file are found in PasArm\pca10040\s132\ses\Output\Release\Exe
if you are mising the Output folder you need to bild the project first.

the .py files are for reading the usb output and ploting in graph. the graph ploting is name sensetiv!