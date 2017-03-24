import com.sun.org.apache.xpath.internal.SourceTree;

import java.io.*;

/**
 * Created by jdonckels on 3/23/17.
 */
public class Main {

    private static String R[] = new String[16];
    private static int numberOfRValues = 0;
    private static String ciphertexts[] = {"36657a5454617064564c4f775a6b6647",
            "00b3bc41acb5427467969c8aba06ac55",
            "c5fb438d525f0ba91e5ba5af7e21a4a4",
            "22b42cb806e5e5de522b3a39e13e38da",
            "199e6892eab1f09a545c568780f8716f",
            "ec150f1323d3506b229038fe82721a9d",
            "2a86a0d5dad7113e29cf4d74f757ee76",
            "7c37971abee9315a0455ed1a58cd4f32",
            "5f8bce8b617b95371aeaad8df4f3d3b9",
            "bf1c86e7b7790767ffed5b3dd6e34149",
            "6d066994cc56127c6b909afec45c2788"};

    private static char IV[];
    private static String currentText;
    private static String currentIV;
    private static String output;

    public static void main (String[] args)
    {


        for(int f = 0; f < ciphertexts.length - 2; f++) {
            currentText = ciphertexts[f + 1];
            currentIV = ciphertexts[f];
            IV = currentIV.toCharArray();

            for (int j = 15; j >= 0; j--) {
                for (int i = 0; i < 256; i++) {

                    String IVinput = "";
                    String hex = "";

                    if( f != ciphertexts.length - 3)
                    {
                        if (i < 16) {
                            hex = Integer.toHexString(i);
                            IV[30 - (numberOfRValues * 2)] = '0';
                            IV[31 - (numberOfRValues * 2)] = hex.charAt(0);
                        } else {
                            hex = Integer.toHexString(i);
                            IV[30 - (numberOfRValues * 2)] = hex.charAt(0);
                            IV[31 - (numberOfRValues * 2)] = hex.charAt(1);
                        }
                    }
                    else
                    {
                        int value = Integer.parseInt(currentIV.substring(30 - (numberOfRValues * 2), 31 - (numberOfRValues * 2)), 16);
                        if (i < 16 && i != value) {
                            hex = Integer.toHexString(i);
                            IV[30 - (numberOfRValues * 2)] = '0';
                            IV[31 - (numberOfRValues * 2)] = hex.charAt(0);
                        } else if(i != value) {
                            hex = Integer.toHexString(i);
                            IV[30 - (numberOfRValues * 2)] = hex.charAt(0);
                            IV[31 - (numberOfRValues * 2)] = hex.charAt(1);
                        }
                    }

                    for (char c : IV) IVinput += String.valueOf(c);

                    if (runServer(IVinput + currentText)) {
                        int value = Integer.parseInt(hex, 16);
                        numberOfRValues++;
                        //Right now it does not add a zero when the value is below 16
                        if (value < 15 && j != 0) {
                            String holder = "0";
                            R[j] = holder + Integer.toHexString(value ^ (16 - j));
                        } else {
                            R[j] = Integer.toHexString(value ^ (16 - j));
                        }
                        updateIV();
                        //System.out.println(IVinput);
                        //System.out.print("R = ");
                        //for (String k : R) System.out.print(" " + k);
                        //System.out.println();
                        break;
                    }
                }
            }

            String finalHexString = "";


            for (int i = 0; i < 32; i += 2) {
                int valueFromR = Integer.parseInt(R[i / 2], 16);
                String hexFromOriginalIV = currentIV.substring(i, i + 2);
                int valueFromIV = Integer.parseInt(hexFromOriginalIV, 16);
                String finalHex = Integer.toHexString(valueFromR ^ valueFromIV);
                finalHexString += finalHex;
            }

            numberOfRValues = 0;
            R = new String[16];

            output += convertHexToString(finalHexString);
            System.out.println(output);
        }
        System.out.println(output);
    }

    public static void updateIV()
    {
        for(int i = 0; i < numberOfRValues; i++)
        {
            int value = Integer.parseInt(R[15 - i], 16);
            value = value ^ (numberOfRValues + 1);
            String hex = Integer.toHexString(value);
            if (value < 16) {
                IV[30 - (i * 2)] = '0';
                IV[31 - (i * 2)] = hex.charAt(0);
            } else {
                IV[30 - (i * 2)] = hex.charAt(0);
                IV[31 - (i * 2)] = hex.charAt(1);
            }
        }

    }

    public static String convertHexToString(String hex){

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);

            temp.append(decimal);
        }

        return sb.toString();
    }

    public static boolean runServer(String input)
    {
        String s = null;
        String command = "python /nfs/student/j/jdonckels/IdeaProjects/Joshua_Donckels_Cipher/src/client.py -ip shasta.cs.unm.edu" +
                " -p 10024 -b " + input + " -id 24";
        try {
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            while ((s = stdError.readLine()) != null) {
                //System.out.println(s);
                if(s.contains("successfully"))
                {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
