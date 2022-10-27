/*
 * Copyright (C) 2022 TianFeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.limt.rxble

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 * @author tianfeng
 */
object GattAttributes {
    private val attributes: HashMap<String, String> = HashMap()

    fun lookup(uuid: String?, defaultName: String): String {
        val name = attributes[uuid]
        return name ?: defaultName
    }

    init {
        // Sample Services.
        attributes["0000180d-0000-1000-8000-00805f9b34fb"] = "Heart Rate Service"
        attributes["0000180a-0000-1000-8000-00805f9b34fb"] = "Device Information Service"

        // Sample Characteristics.
        attributes["00002a37-0000-1000-8000-00805f9b34fb"] = "Heart Rate Measurement"
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"

        // GATT Services
        attributes["00001800-0000-1000-8000-00805f9b34fb"] = "Generic Access"
        attributes["00001801-0000-1000-8000-00805f9b34fb"] = "Generic Attribute"

        // GATT Declarations
        attributes["00002800-0000-1000-8000-00805f9b34fb"] = "Primary Service"
        attributes["00002801-0000-1000-8000-00805f9b34fb"] = "Secondary Service"
        attributes["00002802-0000-1000-8000-00805f9b34fb"] = "Include"
        attributes["00002803-0000-1000-8000-00805f9b34fb"] = "Characteristic"

        // GATT Descriptors
        attributes["00002900-0000-1000-8000-00805f9b34fb"] = "Characteristic Extended Properties"
        attributes["00002901-0000-1000-8000-00805f9b34fb"] = "Characteristic User Description"
        attributes["00002902-0000-1000-8000-00805f9b34fb"] = "Client Characteristic Configuration"
        attributes["00002903-0000-1000-8000-00805f9b34fb"] = "Server Characteristic Configuration"
        attributes["00002904-0000-1000-8000-00805f9b34fb"] = "Characteristic Presentation Format"
        attributes["00002905-0000-1000-8000-00805f9b34fb"] = "Characteristic Aggregate Format"
        attributes["00002906-0000-1000-8000-00805f9b34fb"] = "Valid Range"
        attributes["00002907-0000-1000-8000-00805f9b34fb"] = "External Report Reference Descriptor"
        attributes["00002908-0000-1000-8000-00805f9b34fb"] = "Report Reference Descriptor"

        // GATT Characteristics
        attributes["00002a00-0000-1000-8000-00805f9b34fb"] = "Device Name"
        attributes["00002a01-0000-1000-8000-00805f9b34fb"] = "Appearance"
        attributes["00002a02-0000-1000-8000-00805f9b34fb"] = "Peripheral Privacy Flag"
        attributes["00002a03-0000-1000-8000-00805f9b34fb"] = "Reconnection Address"
        attributes["00002a04-0000-1000-8000-00805f9b34fb"] = "PPCP"
        attributes["00002a05-0000-1000-8000-00805f9b34fb"] = "Service Changed"

        // GATT Service UUIDs
        attributes["00001802-0000-1000-8000-00805f9b34fb"] = "Immediate Alert"
        attributes["00001803-0000-1000-8000-00805f9b34fb"] = "Link Loss"
        attributes["00001804-0000-1000-8000-00805f9b34fb"] = "Tx Power"
        attributes["00001805-0000-1000-8000-00805f9b34fb"] = "Current Time Service"
        attributes["00001806-0000-1000-8000-00805f9b34fb"] = "Reference Time Update Service"
        attributes["00001807-0000-1000-8000-00805f9b34fb"] = "Next DST Change Service"
        attributes["00001808-0000-1000-8000-00805f9b34fb"] = "Glucose"
        attributes["00001809-0000-1000-8000-00805f9b34fb"] = "Health Thermometer"
        attributes["0000180a-0000-1000-8000-00805f9b34fb"] = "Device Information"
        attributes["0000180b-0000-1000-8000-00805f9b34fb"] = "Network Availability"
        attributes["0000180d-0000-1000-8000-00805f9b34fb"] = "Heart Rate"
        attributes["0000180e-0000-1000-8000-00805f9b34fb"] = "Phone Alert Status Service"
        attributes["0000180f-0000-1000-8000-00805f9b34fb"] = "Battery Service"
        attributes["00001810-0000-1000-8000-00805f9b34fb"] = "Blood Pressure"
        attributes["00001811-0000-1000-8000-00805f9b34fb"] = "Alert Notification Service"
        attributes["00001812-0000-1000-8000-00805f9b34fb"] = "Human Interface Device"
        attributes["00001813-0000-1000-8000-00805f9b34fb"] = "Scan Parameters"
        attributes["00001814-0000-1000-8000-00805f9b34fb"] = "Running Speed and Cadence"
        attributes["00001816-0000-1000-8000-00805f9b34fb"] = "Cycling Speed and Cadence"
        attributes["00001818-0000-1000-8000-00805f9b34fb"] = "Cycling Power"
        attributes["00001819-0000-1000-8000-00805f9b34fb"] = "Location and Navigation"

        // GATT Characteristic UUIDs
        attributes["00002a06-0000-1000-8000-00805f9b34fb"] = "Alert Level"
        attributes["00002a07-0000-1000-8000-00805f9b34fb"] = "Tx Power Level"
        attributes["00002a08-0000-1000-8000-00805f9b34fb"] = "Date Time"
        attributes["00002a09-0000-1000-8000-00805f9b34fb"] = "Day of Week"
        attributes["00002a0a-0000-1000-8000-00805f9b34fb"] = "Day Date Time"
        attributes["00002a0c-0000-1000-8000-00805f9b34fb"] = "Exact Time 256"
        attributes["00002a0d-0000-1000-8000-00805f9b34fb"] = "DST Offset"
        attributes["00002a0e-0000-1000-8000-00805f9b34fb"] = "Time Zone"
        attributes["00002a0f-0000-1000-8000-00805f9b34fb"] = "Local Time Information"
        attributes["00002a11-0000-1000-8000-00805f9b34fb"] = "Time with DST"
        attributes["00002a12-0000-1000-8000-00805f9b34fb"] = "Time Accuracy"
        attributes["00002a13-0000-1000-8000-00805f9b34fb"] = "Time Source"
        attributes["00002a14-0000-1000-8000-00805f9b34fb"] = "Reference Time Information"
        attributes["00002a16-0000-1000-8000-00805f9b34fb"] = "Time Update Control Point"
        attributes["00002a17-0000-1000-8000-00805f9b34fb"] = "Time Update State"
        attributes["00002a18-0000-1000-8000-00805f9b34fb"] = "Glucose Measurement"
        attributes["00002a19-0000-1000-8000-00805f9b34fb"] = "Battery Level"
        attributes["00002a1c-0000-1000-8000-00805f9b34fb"] = "Temperature Measurement"
        attributes["00002a1d-0000-1000-8000-00805f9b34fb"] = "Temperature Type"
        attributes["00002a1e-0000-1000-8000-00805f9b34fb"] = "Intermediate Temperature"
        attributes["00002a21-0000-1000-8000-00805f9b34fb"] = "Measurement Interval"
        attributes["00002a22-0000-1000-8000-00805f9b34fb"] = "Boot Keyboard Input Report"
        attributes["00002a23-0000-1000-8000-00805f9b34fb"] = "System ID"
        attributes["00002a24-0000-1000-8000-00805f9b34fb"] = "Model Number String"
        attributes["00002a25-0000-1000-8000-00805f9b34fb"] = "Serial Number String"
        attributes["00002a26-0000-1000-8000-00805f9b34fb"] = "Firmware Revision String"
        attributes["00002a27-0000-1000-8000-00805f9b34fb"] = "Hardware Revision String"
        attributes["00002a28-0000-1000-8000-00805f9b34fb"] = "Software Revision String"
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"
        attributes["00002a2a-0000-1000-8000-00805f9b34fb"] =
            "IEEE 11073-20601 Regulatory Certification Data List"
        attributes["00002a2b-0000-1000-8000-00805f9b34fb"] = "Current Time"
        attributes["00002a31-0000-1000-8000-00805f9b34fb"] = "Scan Refresh"
        attributes["00002a32-0000-1000-8000-00805f9b34fb"] = "Boot Keyboard Output Report"
        attributes["00002a33-0000-1000-8000-00805f9b34fb"] = "Boot Mouse Input Report"
        attributes["00002a34-0000-1000-8000-00805f9b34fb"] = "Glucose Measurement Context"
        attributes["00002a35-0000-1000-8000-00805f9b34fb"] = "Blood Pressure Measurement"
        attributes["00002a36-0000-1000-8000-00805f9b34fb"] = "Intermediate Cuff Pressure"
        attributes["00002a37-0000-1000-8000-00805f9b34fb"] = "Heart Rate Measurement"
        attributes["00002a38-0000-1000-8000-00805f9b34fb"] = "Body Sensor Location"
        attributes["00002a39-0000-1000-8000-00805f9b34fb"] = "Heart Rate Control Point"
        attributes["00002a3e-0000-1000-8000-00805f9b34fb"] = "Network Availability"
        attributes["00002a3f-0000-1000-8000-00805f9b34fb"] = "Alert Status"
        attributes["00002a40-0000-1000-8000-00805f9b34fb"] = "Ringer Control Point"
        attributes["00002a41-0000-1000-8000-00805f9b34fb"] = "Ringer Setting"
        attributes["00002a42-0000-1000-8000-00805f9b34fb"] = "Alert Category ID Bit Mask"
        attributes["00002a43-0000-1000-8000-00805f9b34fb"] = "Alert Category ID"
        attributes["00002a44-0000-1000-8000-00805f9b34fb"] = "Alert Notification Control Point"
        attributes["00002a45-0000-1000-8000-00805f9b34fb"] = "Unread Alert Status"
        attributes["00002a46-0000-1000-8000-00805f9b34fb"] = "New Alert"
        attributes["00002a47-0000-1000-8000-00805f9b34fb"] = "Supported New Alert Category"
        attributes["00002a48-0000-1000-8000-00805f9b34fb"] = "Supported Unread Alert Category"
        attributes["00002a49-0000-1000-8000-00805f9b34fb"] = "Blood Pressure Feature"
        attributes["00002a4a-0000-1000-8000-00805f9b34fb"] = "HID Information"
        attributes["00002a4b-0000-1000-8000-00805f9b34fb"] = "Report Map"
        attributes["00002a4c-0000-1000-8000-00805f9b34fb"] = "HID Control Point"
        attributes["00002a4d-0000-1000-8000-00805f9b34fb"] = "Report"
        attributes["00002a4e-0000-1000-8000-00805f9b34fb"] = "Protocol Mode"
        attributes["00002a4f-0000-1000-8000-00805f9b34fb"] = "Scan Interval Window"
        attributes["00002a50-0000-1000-8000-00805f9b34fb"] = "PnP ID"
        attributes["00002a51-0000-1000-8000-00805f9b34fb"] = "Glucose Feature"
        attributes["00002a52-0000-1000-8000-00805f9b34fb"] = "Record Access Control Point"
        attributes["00002a53-0000-1000-8000-00805f9b34fb"] = "RSC Measurement"
        attributes["00002a54-0000-1000-8000-00805f9b34fb"] = "RSC Feature"
        attributes["00002a55-0000-1000-8000-00805f9b34fb"] = "SC Control Point"
        attributes["00002a5b-0000-1000-8000-00805f9b34fb"] = "CSC Measurement"
        attributes["00002a5c-0000-1000-8000-00805f9b34fb"] = "CSC Feature"
        attributes["00002a5d-0000-1000-8000-00805f9b34fb"] = "Sensor Location"
        attributes["00002a63-0000-1000-8000-00805f9b34fb"] = "Cycling Power Measurement"
        attributes["00002a64-0000-1000-8000-00805f9b34fb"] = "Cycling Power Vector"
        attributes["00002a65-0000-1000-8000-00805f9b34fb"] = "Cycling Power Feature"
        attributes["00002a66-0000-1000-8000-00805f9b34fb"] = "Cycling Power Control Point"
        attributes["00002a67-0000-1000-8000-00805f9b34fb"] = "Location and Speed"
        attributes["00002a68-0000-1000-8000-00805f9b34fb"] = "Navigation"
        attributes["00002a69-0000-1000-8000-00805f9b34fb"] = "Position Quality"
        attributes["00002a6a-0000-1000-8000-00805f9b34fb"] = "LN Feature"
        attributes["00002a6b-0000-1000-8000-00805f9b34fb"] = "LN Control Point"
    }
}