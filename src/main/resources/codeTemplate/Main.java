import java.lang.reflect.Method;
import java.util.*;

public class Main {
    private static final Map<String, String> map = new HashMap<>();

    //列举所有的参数类型
    static {
        map.put("int", "int");
        map.put("java.lang.String", "String");
        map.put("char", "char");
        map.put("boolean", "boolean");
        map.put("float", "float");
        map.put("double", "double");
        map.put("long", "long");
        map.put("short", "short");
        map.put("byte", "byte");
        map.put("[I", "int[]");
        map.put("[Ljava.lang.String;", "String[]");
        map.put("[C", "char[]");
        map.put("[Z", "boolean[]");
        map.put("[F", "float[]");
        map.put("[D", "double[]");
        map.put("[J", "long[]");
        map.put("[S", "short[]");
        map.put("[B", "byte[]");
        map.put("[[I", "int[][]");
        map.put("[[Ljava.lang.String;", "String[][]");
        map.put("[[C", "char[][]");
        map.put("[[Z", "boolean[][]");
        map.put("[[F", "float[][]");
        map.put("[[D", "double[][]");
        map.put("[[J", "long[][]");
        map.put("[[S", "short[][]");
        map.put("[[B", "byte[][]");
        map.put("TreeNode", "TreeNode");
    }

    public static void main(String[] args) {
        Solution solution = new Solution();
        Class<? extends Solution> aClass = solution.getClass();
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("solution")) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                ArrayList<Object> arrayList = new ArrayList<>();
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    String paramType = map.get(parameterType.getName());
                    if ( paramType != null) {
                        switch (paramType){
                            case "int":
                                arrayList.add(Integer.parseInt(args[i]));
                                break;
                            case "String":
                                arrayList.add(args[i]);
                                break;
                            case "char":
                                arrayList.add(args[i].charAt(0));
                                break;
                            case "boolean":
                                arrayList.add(Boolean.parseBoolean(args[i]));
                                break;
                            case "float":
                                arrayList.add(Float.parseFloat(args[i]));
                                break;
                            case "double":
                                arrayList.add(Double.parseDouble(args[i]));
                                break;
                            case "long":
                                arrayList.add(Long.parseLong(args[i]));
                                break;
                            case "int[]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(",")).mapToInt(Integer::parseInt).toArray());
                                break;
                            case "String[]":
                                arrayList.add(args[i].substring(1, args[i].length() - 1).split(","));
                                break;
                            case "char[]":
                                arrayList.add(args[i].substring(1, args[i].length() - 1).replace("[", "").replace("]", "").replace(",","").toCharArray());
                                break;
                            case "boolean[]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(",")).map(Boolean::parseBoolean).toArray(Boolean[]::new));
                                break;
                            case "float[]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(",")).map(Float::parseFloat).toArray(Float[]::new));
                                break;
                            case "double[]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(",")).mapToDouble(Double::parseDouble).toArray());
                                break;
                            case "long[]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(",")).mapToLong(Long::parseLong).toArray());
                                break;
                            case "int[][]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(";")).map(s1 -> Arrays.stream(s1.split(",")).mapToInt(Integer::parseInt).toArray()).toArray(int[][]::new));
                                break;
                            case "String[][]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(";")).map(s1 -> s1.split(",")).toArray(String[][]::new));
                                break;
                            case "char[][]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(";")).map(s1 -> s1.replace("[", "").replace("]", "").replace(",","").toCharArray()).toArray(char[][]::new));
                                break;
                            case "boolean[][]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(";")).map(s1 -> Arrays.stream(s1.split(",")).map(Boolean::parseBoolean).toArray(Boolean[]::new)).toArray(boolean[][]::new));
                                break;
                            case "float[][]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(";")).map(s1 -> Arrays.stream(s1.split(",")).map(Float::parseFloat).toArray(Float[]::new)).toArray(float[][]::new));
                                break;
                            case "double[][]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(";")).map(s1 -> Arrays.stream(s1.split(",")).mapToDouble(Double::parseDouble).toArray()).toArray(double[][]::new));
                                break;
                            case "long[][]":
                                arrayList.add(Arrays.stream(args[i].substring(1, args[i].length() - 1).split(";")).map(s1 -> Arrays.stream(s1.split(",")).mapToLong(Long::parseLong).toArray()).toArray(long[][]::new));
                                break;
                            case "TreeNode":
                                // todo 设置TreeNode
                                break;
                            default:
                                break;
                        }
                    }
                }
                // 调用 solution
                try {
                    Object invoke = method.invoke(solution, arrayList.toArray());
                    Class<?> returnType = method.getReturnType();
                    String returnName = map.get(returnType.getName());
                    if (returnName != null) {
                        switch (returnName){
                            case "int":
                                System.out.println((int)invoke);
                                break;
                            case "String":
                                System.out.println((String)invoke);
                                break;
                            case "char":
                                System.out.println((char)invoke);
                                break;
                            case "boolean":
                                System.out.println((boolean)invoke);
                                break;
                            case "float":
                                System.out.println((float)invoke);
                                break;
                            case "double":
                                System.out.println((double)invoke);
                                break;
                            case "long":
                                System.out.println((long)invoke);
                                break;
                            case "int[]":
                                System.out.println(Arrays.toString((int[])invoke));
                                break;
                            case "String[]":
                                System.out.println(Arrays.toString((String[])invoke));
                                break;
                            case "char[]":
                                System.out.println(Arrays.toString((char[])invoke));
                                break;
                            case "boolean[]":
                                System.out.println(Arrays.toString((boolean[])invoke));
                                break;
                            case "float[]":
                                System.out.println(Arrays.toString((float[])invoke));
                                break;
                            case "double[]":
                                System.out.println(Arrays.toString((double[])invoke));
                                break;
                            case "long[]":
                                System.out.println(Arrays.toString((long[])invoke));
                                break;
                            case "int[][]":
                                System.out.println(Arrays.deepToString((int[][])invoke));
                                break;
                            case "String[][]":
                                System.out.println(Arrays.deepToString((String[][])invoke));
                                break;
                            case "char[][]":
                                System.out.println(Arrays.deepToString((char[][])invoke));
                                break;
                            case "boolean[][]":
                                System.out.println(Arrays.deepToString((boolean[][])invoke));
                                break;
                            case "float[][]":
                                System.out.println(Arrays.deepToString((float[][])invoke));
                                break;
                            case "double[][]":
                                System.out.println(Arrays.deepToString((double[][])invoke));
                                break;
                            case "long[][]":
                                System.out.println(Arrays.deepToString((long[][])invoke));
                                break;
                            case "TreeNode":
                                // todo 设置TreeNode
                                break;
                            default:
                                break;
                        }
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

// 自定义二叉树
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode() {
    }

    TreeNode(int val) {
        this.val = val;
    }

    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}


// 自定义链表
class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}



// 占位符，以后读取文件替换
[wojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwojwoj]

