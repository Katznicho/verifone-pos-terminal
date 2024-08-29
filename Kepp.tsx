// test scanner -- end

    // test ctls -- start
    //S50卡
    public final static int S50_CARD = 0x00;
    //S70卡
    public final static int S70_CARD = 0x01;
    //PRO卡
    public final static int PRO_CARD = 0x02;
    //支持S50驱动与PRO驱动的PRO卡
    public final static int S50_PRO_CARD = 0x03;
    //支持S70驱动与PRO驱动的PRO卡
    public final static int S70_PRO_CARD = 0x04;
    //CPU卡
    public final static int CPU_CARD = 0x05;


    public void readRFData() {
        Message msg = new Message();
        byte[] key = new byte[6];
        Arrays.fill(key, (byte) 0xFF);

        String DEFAULT_KEY_A = "A0A1A2A3A4A5";
        String DEFAULT_KEY_B = "FFFFFFFFFFFF";
        byte[] apdu_ret = new byte[16];

        try {
            String cardNumber =null;
            for (int i = 0; i < 2; i++) {
                //int ret = irfCardReader.authSector(i, 0, hexStr2Byte(DEFAULT_KEY_A));
                int ret = irfCardReader.authSector(i, 1, hexStr2Byte(DEFAULT_KEY_B));
                irfCardReader.authSector(i, 1, key);
                if (ret != 0) {
                    Log.d(TAG, "Sector " + i + " FAILS: " + ret);
                    continue;
                }

                Log.d(TAG, "Sector " + i + " OK: " + ret);
               String sectordata =  readBlocks(i * 4, msg);
                Log.d("CombinedSectordata", sectordata);
                String sectordataInAscii = hexToAscii(sectordata);
                System.out.println("sectordataInAscii: " + sectordataInAscii);
                 cardNumber = extractCardNumber(sectordataInAscii);
                System.out.println("Extracted card number: " + cardNumber);

            }
            if(cardNumber != null){
                updateUI("CARD NUMBER: " + cardNumber);
            }else{
                updateUI("CARD NOT FOUND" );
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String readBlocks(int blockNumberOffset, Message msg) throws RemoteException {
       StringBuilder sectordata = new StringBuilder();
        for (int j = 0; j < 4; j++) {
            byte[] buffer = new byte[16];
            int ret = irfCardReader.readBlock(j + blockNumberOffset, buffer);

            if (ret == 0) {
                sectordata.append(byte2HexStr2(buffer));
                Log.d(TAG, "readData For Block(" + (j + blockNumberOffset) + "): success: " + byte2HexStr2(buffer));
                msg.getData().putString("msg", "readData For Block(" + (j + blockNumberOffset) + "): success: " + byte2HexStr2(buffer));
                //updateUI("RFID DATA: " + byte2HexStr2(buffer));
            } else {
                Log.d(TAG, "readData: fail: " + ret + " @ " + (j + blockNumberOffset));
            }
        }
        if(sectordata == null){
            return null;
        }
        return sectordata.toString();
    }

    public static String extractCardNumber(String input) {
        // Define a regex pattern to match the card number format
        String regex = "SU\\d+";  // Assumes 'SU' followed by digits

        // Compile the pattern and create a matcher
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // Find the first match
        if (matcher.find()) {
            return matcher.group();  // Return the matched substring
        }

        return null;  // Return null if no match found
    }

    public static String hexToAscii(String hexString) {
        StringBuilder asciiStringBuilder = new StringBuilder();

        // Iterate over the hex string in steps of 2 (each pair represents a byte)
        for (int i = 0; i < hexString.length(); i += 2) {
            // Extract the current byte in hex
            String hexByte = hexString.substring(i, i + 2);

            // Convert the hex byte to an integer
            int decimal = Integer.parseInt(hexByte, 16);

            // Convert the decimal value to its corresponding ASCII character
            asciiStringBuilder.append((char) decimal);
        }

        return asciiStringBuilder.toString();
    }