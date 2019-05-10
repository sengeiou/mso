/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.uia.mso_login;

import java.util.HashMap;

public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    // These attributes were found using Nordic Semiconductor`s 'nRF Connect' app
    // Attributes specific for Nordic`s ble_app_uart example project (C:\nRF5_SDK_15\examples\ble_peripheral\ble_app_uart)

    // Service #1
    public static String GENERIC_ACCESS =                               "00001800-0000-1000-8000-00805f9b34fb";
    // Characteristics
    public static String DEVICE_NAME =                                  "00002a00-0000-1000-8000-00805f9b34fb";
    public static String APPEARANCE =                                   "00002a01-0000-1000-8000-00805f9b34fb";
    public static String PERIPHERAL_PREFERRED_CONNECTION_PARAMETER =    "00002a04-0000-1000-8000-00805f9b34fb";
    public static String CENTRAL_ADDRESS_RESOLUTION =                   "00002aa6-0000-1000-8000-00805f9b34fb";

    // Service #2
    public static String GENERIC_ATTRIBUTE =                            "00001801-0000-1000-8000-00805f9b34fb";

    // Service #3
    public static String NORDIC_UART_SERVICE =                          "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    // Characteristics
    public static String RX_CHARACTERISTICS =                           "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static String TX_CHARACTERISTICS =                           "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    // Client Characteristics Configuration for TX channel
    public static String CLIENT_CHARACTERISTIC_CONFIGURATION =          "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Service #1
        attributes.put(GENERIC_ACCESS,                                  "Generic Access");
        // Characteristics
        attributes.put(DEVICE_NAME,                                     "Device Name");
        attributes.put(APPEARANCE,                                      "Appearance");
        attributes.put(PERIPHERAL_PREFERRED_CONNECTION_PARAMETER,       "Peripheral Preferred Connection Parameter");
        attributes.put(CENTRAL_ADDRESS_RESOLUTION,                      "Central Address Resolution");

        // Service #2
        attributes.put(GENERIC_ATTRIBUTE,                               "Generic Attribute");

        // Service #3
        attributes.put(NORDIC_UART_SERVICE,                             "Nordic UART Service");
        // Characteristics
        attributes.put(RX_CHARACTERISTICS,                              "RX Characteristics");
        attributes.put(TX_CHARACTERISTICS,                              "TX Characteristics");
        // Client Characteristics Configuration for TX channel
        attributes.put(CLIENT_CHARACTERISTIC_CONFIGURATION,             "Client Characteristics Configuration");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
