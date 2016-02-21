/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Class to manage the bluetooth communications. The Android app sends commands to us. We process those
 * commands and then send a reply. To make it easy for us to read commands they are a fixed 25-byte length.
 * The responses are variable size. Here's the request/response format for all commands
 *
 *   1. Get measured frequency
 *     Request:
 *       [0     ] : 0x27
 *       [1..4  ] : request sequence number
 *       [5..12 ] : don't care
 *
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : request sequence number
 *       [6..9  ] : sample sequence number
 *       [10..13] : reference frequency
 *       [14..17] : reference counter
 *       [18..21] : sample counter
 *       [22..25] : gate counter
 *       [26..27] : MCU internal temperature (mC)
 *
 *   2. Set gate counter
 *     Request:
 *       [0     ] : 0x28
 *       [1..4  ] : request sequence number
 *       [5..8  ] : new gate counter
 *       [9..12 ] : don't care
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : request sequence number
 *
 *   3. Set sample clock filter bandwidth
 *     Request:
 *       [0     ] : 0x29
 *       [1..4  ] : request sequence number
 *       [5     ] : 0=full, 1=500MHz, 2=160MHz, 3=50MHz
 *       [6..12 ] : don't care
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : request sequence number
 *
 *   4. Set external reference clock filter bandwidth
 *     Request:
 *       [0]    ] : 0x2a
 *       [1..4  ] : request sequence number
 *       [5     ] : 0=full, 1=500MHz, 2=160MHz, 3=50MHz
 *       [6..12 ] : don't care
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : request sequence number
 *
 *   5. Set the reference clock source
 *     Request:
 *       [0     ] : 0x2b
 *       [1..4  ] : request sequence number
 *       [5     ] : 0=onboard 10MHz, 1=external 10MHz
 *       [6..12 ] : don't care
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : request sequence number
 *
 *    6. Reset the PLL
 *     Request:
 *       [0     ] : 0x2c
 *       [1..4  ] : request sequence number
 *       [5..12 ] : don't care
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : request sequence number
 *
 *    6. Reset the MCU and PLL
 *     Request:
 *       [0     ] : 0x2d
 *       [1..4  ] : request sequence number
 *       [5..12 ] : don't care
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : request sequence number
 *
 *    7. Get calibration
 *     Request
 *       [0     ] : 0x2e
 *       [1..4  ] : request sequence number
 *       [5..6  ] : calibration entry number
 *       [7..12 ] : don't care
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : Request sequence number
 *       [6..23 ] : Calibration entry (18 bytes)
 *
 *    8. Set calibration
 *     Request
 *       [0     ] : 0x2f
 *       [1..4  ] : request sequence number
 *       [5..6  ] : calibration entry number
 *       [7..24 ] : calibration structure
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : Request sequence number
 *
 *     9. Get calibration bitmap
 *      Request
 *       [0     ] : 0x30
 *       [1..4  ] : request sequence number
 *       [5..12 ] : don't care
 *     Response:
 *       [0     ] : Command id
 *       [1     ] : 0=OK, 1..255 = error
 *       [2..5  ] : request sequence number
 *       [6..20 ] : 113 bit vector. [0]=cal slot 0, [112]=cal slot 112. 1=used, 0=available
 */

DECLARE_EVENT_SIGNATURE(RequestReceived,void(const Request&));

class BluetoothCommunicator {

  protected:
    typedef Usart1_Remap1<> MyUsart;
    typedef Usart1TxDmaChannel<
        UsartDmaWriterFeature<Usart1PeripheralTraits>,
        Usart1TxDmaChannelInterruptFeature
    > MyDmaWriter;

    /*
     * The RX channel is circular so we continue to receive commands
     */

    typedef Usart1RxDmaChannel<
        UsartDmaReaderFeature<Usart1PeripheralTraits,DMA_Priority_High,DMA_Mode_Circular>,
        Usart1RxDmaChannelInterruptFeature
    > MyDmaReader;

    MyUsart _usart;             // usart must be declared before the dma reader/writer variables
    MyDmaReader _dmaReader;
    MyDmaWriter _dmaWriter;

    uint8_t _request[Request::REQUEST_BYTE_SIZE];
    const uint8_t *_response;

  protected:
    void onRxComplete(DmaEventType det);
    void onTxComplete(DmaEventType det);

  public:
    BluetoothCommunicator();

    DECLARE_EVENT_SOURCE(RequestReceived);

    void start();

    void sendResponse(const uint8_t *response,uint16_t size);
};


/*
 * Constructor
 */

inline BluetoothCommunicator::BluetoothCommunicator()
  : _usart(9600) {

  // subscribe to the DMA TX complete interrupt

  _dmaWriter.DmaInterruptEventSender.insertSubscriber(
      DmaInterruptEventSourceSlot::bind(this,&BluetoothCommunicator::onTxComplete)
  );

  // subscribe to the DMA RX complete interrupt

  _dmaReader.DmaInterruptEventSender.insertSubscriber(
      DmaInterruptEventSourceSlot::bind(this,&BluetoothCommunicator::onRxComplete)
  );

  // lower the priority to below systick

  _dmaReader.Usart1RxDmaChannelInterruptFeature::setNvicPriorities(1);
  _dmaWriter.Usart1TxDmaChannelInterruptFeature::setNvicPriorities(1);

  // enable the DMA complete interrupts

  _dmaReader.enableInterrupts(Usart1RxDmaChannelInterruptFeature::COMPLETE);
  _dmaWriter.enableInterrupts(Usart1TxDmaChannelInterruptFeature::COMPLETE);
}


/*
 * Start the request/response loop
 */

inline void BluetoothCommunicator::start() {

  // start a DMA read, we'll get an interrupt when it's done

  _dmaReader.beginRead(_request,sizeof(_request));
}


/*
 * Send a response
 */

inline void BluetoothCommunicator::sendResponse(const uint8_t *response,uint16_t size) {

  // start sending by DMA

  _response=response;
  _dmaWriter.beginWrite(_response,size);
}


/*
 * DMA RX complete
 */

inline void BluetoothCommunicator::onRxComplete(DmaEventType det) {

  if(det==DmaEventType::EVENT_COMPLETE) {

    const Request *request;

    // if the request is good then notify subscribers

    request=reinterpret_cast<const Request *>(_request);
    if(request->isValid()) {
      RequestReceivedEventSender.raiseEvent(*request);
    }
  }
}


/*
 * DMA TX complete
 */

inline void BluetoothCommunicator::onTxComplete(DmaEventType det) {

  if(det==DmaEventType::EVENT_COMPLETE) {
  }
}
