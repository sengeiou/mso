/**
 * Copyright (c) 2014 - 2018, Nordic Semiconductor ASA
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form, except as embedded into a Nordic
 *    Semiconductor ASA integrated circuit in a product or a software update for
 *    such product, must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. Neither the name of Nordic Semiconductor ASA nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * 4. This software, with or without modification, must only be used with a
 *    Nordic Semiconductor ASA integrated circuit.
 *
 * 5. Any software provided in binary form under this license must not be reverse
 *    engineered, decompiled, modified and/or disassembled.
 *
 * THIS SOFTWARE IS PROVIDED BY NORDIC SEMICONDUCTOR ASA "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY, NONINFRINGEMENT, AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NORDIC SEMICONDUCTOR ASA OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
#ifndef COSTOM_BOARD_H
#define COSTOM_BOARD_H

#ifdef __cplusplus
extern "C" {
#endif

//#define PasArm_CUSTOM_BOARD   1
#define PasArm_BOARD_PCA10040 1

#include "nrf_gpio.h"

#if defined(PasArm_CUSTOM_BOARD)
  // LEDs definitions for ACN52832
  #define LEDS_NUMBER    3

  #define LED_START      22
  #define LED_1          22   // RED
  #define LED_2          23   // BLUE
  #define LED_3          24   // GREEN
  #define LED_STOP       24

  #define LEDS_ACTIVE_STATE 0

  #define LEDS_INV_MASK  LEDS_MASK

  #define LEDS_LIST { LED_1, LED_2, LED_3 }

  #define BSP_LED_0      LED_1
  #define BSP_LED_1      LED_2
  #define BSP_LED_2      LED_3

  // BUTTONs definitions for PasArm_BOARD
  #define BUTTONS_NUMBER 3

  #define BUTTON_START   6
  #define BUTTON_1       6
  #define BUTTON_2       7
  #define BUTTON_3       8
  #define BUTTON_STOP    8
  #define BUTTON_PULL    NRF_GPIO_PIN_PULLUP

  #define BUTTONS_ACTIVE_STATE 0

  #define BUTTONS_LIST { BUTTON_1, BUTTON_2, BUTTON_3 }

  #define BSP_BUTTON_0   BUTTON_1
  #define BSP_BUTTON_1   BUTTON_2
  #define BSP_BUTTON_2   BUTTON_3
#elif defined(PasArm_BOARD_PCA10040)
  // LEDs definitions for PCA10040
  #define LEDS_NUMBER    4

  #define LED_START      17
  #define LED_1          17
  #define LED_2          18
  #define LED_3          19
  #define LED_4          20
  #define LED_STOP       20

  #define LEDS_ACTIVE_STATE 0

  #define LEDS_INV_MASK  LEDS_MASK

  #define LEDS_LIST { LED_1, LED_2, LED_3, LED_4 }

  #define BSP_LED_0      LED_1
  #define BSP_LED_1      LED_2
  #define BSP_LED_2      LED_3
  #define BSP_LED_3      LED_4

  #define BUTTONS_NUMBER 4

  #define BUTTON_START   13
  #define BUTTON_1       13
  #define BUTTON_2       14
  #define BUTTON_3       15
  #define BUTTON_4       16
  #define BUTTON_STOP    16
  #define BUTTON_PULL    NRF_GPIO_PIN_PULLUP

  #define BUTTONS_ACTIVE_STATE 0

  #define BUTTONS_LIST { BUTTON_1, BUTTON_2, BUTTON_3, BUTTON_4 }

  #define BSP_BUTTON_0   BUTTON_1
  #define BSP_BUTTON_1   BUTTON_2
  #define BSP_BUTTON_2   BUTTON_3
  #define BSP_BUTTON_3   BUTTON_4
#endif

// serialization APPLICATION board - temp. setup for running serialized MEMU tests
#define SER_APP_RX_PIN              23    // UART RX pin number.
#define SER_APP_TX_PIN              24    // UART TX pin number.
#define SER_APP_CTS_PIN             2     // UART Clear To Send pin number.
#define SER_APP_RTS_PIN             25    // UART Request To Send pin number.

// serialization CONNECTIVITY board
#define SER_CON_RX_PIN              24    // UART RX pin number.
#define SER_CON_TX_PIN              23    // UART TX pin number.
#define SER_CON_CTS_PIN             25    // UART Clear To Send pin number. Not used if HWFC is set to false.
#define SER_CON_RTS_PIN             2     // UART Request To Send pin number. Not used if HWFC is set to false.

#define SER_CONN_CHIP_RESET_PIN     11    // Pin used to reset connectivity chip

// Arduino board mappings
#define ARDUINO_SCL_PIN             27    // SCL signal pin
#define ARDUINO_SDA_PIN             26    // SDA signal pin
#define ARDUINO_AREF_PIN            2     // Aref pin
#define ARDUINO_13_PIN              25    // Digital pin 13
#define ARDUINO_12_PIN              24    // Digital pin 12
#define ARDUINO_11_PIN              23    // Digital pin 11
#define ARDUINO_10_PIN              22    // Digital pin 10
#define ARDUINO_9_PIN               20    // Digital pin 9
#define ARDUINO_8_PIN               19    // Digital pin 8

#define ARDUINO_7_PIN               18    // Digital pin 7
#define ARDUINO_6_PIN               17    // Digital pin 6
#define ARDUINO_5_PIN               16    // Digital pin 5
#define ARDUINO_4_PIN               15    // Digital pin 4
#define ARDUINO_3_PIN               14    // Digital pin 3
#define ARDUINO_2_PIN               13    // Digital pin 2
#define ARDUINO_1_PIN               12    // Digital pin 1
#define ARDUINO_0_PIN               11    // Digital pin 0

#define ARDUINO_A0_PIN              3     // Analog channel 0
#define ARDUINO_A1_PIN              4     // Analog channel 1
#define ARDUINO_A2_PIN              28    // Analog channel 2
#define ARDUINO_A3_PIN              29    // Analog channel 3
#define ARDUINO_A4_PIN              30    // Analog channel 4
#define ARDUINO_A5_PIN              31    // Analog channel 5


#ifdef __cplusplus
}
#endif

#endif // COSTOM_BOARD_H
