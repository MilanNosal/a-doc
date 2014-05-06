
public class Main {

    public static void main(String[] args) {
        System.out.println("////".replaceAll("//", "/"));

        int a = 5;
        some:
        if (true) {
            System.out.println("blabla");
            while (a < 8) {
                System.out.println(a);
                a++;
                if (a == 7) {

                    break some;
                }
            }
        } else {
            System.out.println("rumba");
        }

        System.out.println("fdsfsadfdas");
        //(new HaskellInstantiator("C:\\Users\\milan_000\\Documents\\haskell experiments")).instantiateFor(null, null);
    }
}
