/**
 *
 */
package com.workruit.us.application.constants;

import java.util.Arrays;
import java.util.List;

/**
 * @author Santosh Bhima
 */
public class CommonConstants {
    /**
     * 1) A-Z characters allowed. 2) a-z characters allowed 3) 0-9 numbers allowed
     * 4) Additionally email may contain only dot(.), dash(-) and underscore(_) 5)
     * Rest all characters are not allowed
     */
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    public static final String MOBILE_REGEX = "[0-9]{10}";
    public static final String company_default_image = "iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAASRSURBVHgBzVlNaBtHFH6zP0aO3VQikmqlhUrpoZAU7BJ6aiEy9BR6aKHpMW0uObRQ24eG3mwf+wO1Cw00l9rt0YdQUnoqWIZe2mKQDwoNNJYMxUokG22CHRvv7kzem2jFWpbknWWR88FqR7Oznk/fe/PmzTODkKjX7TyAO8aZfokxNsZAxIWAOD1jDCwBUMHvFRB8jYNWyKTMAoQAUxlcLov48GlnApuTHpngMzEkCwXXtmczmcFK4NeCDGoSm0ZSkxAJ2EJQoscS3N4+mODAZpQVO3ZmVNTls+n0wELPYb0e1rft76JTrQsBBnOpM+ZU1+edOsmkQy84t7GZh36AQXFXM8ZzCWa1P9I6jR867SxDv8gRBIwNuVKQIzhCkMxKL0D/kZdzt+GQiWu1g09AYz/BCYILd2okFZvzvrcIVqt7WX3AXAYhsnCysHZ1I+f5Y8vEumlOPwfkCPFh7kx7X6SCUj3TKEOE2H/iyHvslAFhgComSEWpoFQvQjTq+3BrZk1e1A6DU86BjL9Swdq2U47KvB65vaaCg6jg9ZlRSKRioAgrnTQTrIpZic5gGSLAZmUHbs2uSVJXPn1d9i3dvCfvV7+4AJnsMKjAFTCuacDzEBLkZ6TWn7//D6uFh/D9jdUWud8W78vLI/ojEqcxKmCYzrH6ln0bc7f3QRGeKf0+dhYV+hAJ/fJN6ZCJPcKk8LtXsni9GmgOLvivBtLM4s6hBJrII0H+Vfp7S6r59uVXWia+Pj0qx9I4MjORXF15AKAwmabpo6RgA18JnEqRme4s/idJkF/R5OffSspFsHTzX3n3Lwq/0jSexirAYrUtO/BP+mNpA6+KNOV7H78myflNfO78i3D1xhtHYh+N+eqzv6RpycQq0JRGo3ku5l9qkSMTf/71RTnxO5dfRuXGYLO8A19+tALrdy1YL1myHTYWEgwMhFZQE9Ovv/vPFvz8bUmaeALJkSnP+sJHIh2ThD0T+9shYGmCHU0Se4EWyP6uI51+6Yd7UN3YeRb/0M+ovYfP1kuP5KLxt0OBQUVjQhQhBAaHyM9E8+/Ase0w4JxvGCDcFWC6chyMIcFzF+IyxBCo7S2Obv2qwB9YNBzQizqoo1Hbl6vaDyJDgnXsDwE68Bt04sdYaKnEQgL5HYFiHpGiAF3FPtG0aHu/KnBtVDJJoyC1xy1lnjFNKeWirasddxbudxzbrb8XGFYh5J0+Gg0Rt12nEeRFimnt+y8ptblxWKVO/RRugoYc13ZyVHlonUlwR6GDygQ8F2AL6aRxTba8LlLRcZ2yqi9GDfI9fmCPe3Wb1laXwPzfFWIWThgc6zX+otKhvXgkNUBmnocTAse5M23FpI61GfTH/pY+njEpps+Yb7Z3d8xmTN34gF6A/qGw+9gY7/SgZ/mtHyubzDqSNLuW+Hrmg3jsm3S5uCaLjVED69hciKle5AjHJqzktC4ue8HFIkQEUg1Nmmsuyp5QKqJTiUTT9Rmma5eUD/pU+ecw/2THmMvlguegSgT9oAM/nak1po0KOhmCPB3GPTL4YVGu6WI6JzBjCvtviKfQDDUIfcqIhgAAAABJRU5ErkJggg==";
    public static final String applicant_default_image = "iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAO4SURBVHgBzZk/TFNRFMa/+1oBQUqbQBBDYhl1AePiItTBXZHVRBbXwsBkDBjdHCwjC2Bi4gIYBxNwocLi4FAXGCmGBESS1qKN2PZdz7m0L6X0te9dSusvKa+Pe++73zv3zzn3VECT3UMZEiYGhMAQpLr6JeDnMgEkpUScvsRNia8wEO1pF1FoINxU3kpI/yUvwtT5WEGMC+LUWTSXw7OegIg7beRIIAtr82KShaEGUKfzToVWFbj/S4ZhYkrDYtWImyCRPjFfqVJFgfuH8lWtrGaHIRDpahfjduVlBaoh9eAdWS2EeiARS5u40xcQydIio1z9Vi9W6yaOERhgg5QrOiWQh5W3DdQZNsgP7ruEE0O8m5KPSPEcGgjN+fHLHSJSuLcE7iZk0PBglb4G0ViS6Rz6CvPRGmKPB5NovDiGncFk4UZZMG+9LWiysJbF4noWnzdz6v7WNQ8e3PZiZNALXciKAbaiegJbT0KPiZkjLJC4Ylgofza3TTx92AQdWg21/06pIdbdUiJLmVPiipldyWB2OQstBMJ8MTgqgebcW1yr3vn00l9o4qepFzLIz4agAQ/hzoFZtV4qLbGxrTeBhIEBg3xhP86ZVLr6i9hAFhR6w3v9quG4bm+n87rFUBDcbwipJ9DXKtR2Ug2u09vlKi4uxm+cJc57+bgZvjb7zvkluM4Z8OvZPg9b5sOLi2WHsLdT4O2TlrNYTyH2UzKha8WdA2mJKWzODA8rf3gFp9LH5ZokxfeUZBcXhEt4A37+5kh1PjJ4AXdveqyFw8J5j5xdziiRY8NNCA9fgFtofcRYIAeK99w0ZA/idgPWEUmh13uD/nxy04iHUcc7RKhNYQo4hSZQzJAmYm4aTcxouy4VWLjCRNSgs2mUMwFO6nNY5cS92cFz8+MXh8EDZSZYm5rVOYlpJ20W1zUjkyLmVhw+QyDKFyWwxUTESZuNb/rWs56x7ewZJmUe+GptUHs/ZUTkY7D/gPlunxjlL5bABB3WMxT2n0OKwx0090w6xBfyNpaPClD8n5XHZm0kpjiZVDrhRK/QeVQ6XDDnAfddmkwq6yT3UnJV1DP1wZBb6+4QN0r/XTaaac7hPjdAneDEJiePbMrsqcfK5mGlVIdtiq9iPMgNadca5ZWFGkORSjKfh6mYf6wasPKk5WVPyfDXqBFstd8m+oqTRHa4iiQ5RUKvxKf9IbeHLbYYxTLTf8hrlUtU1kRgMXyoJrEhdWylgxc9KChF/mcIHj4OQARiHM5xxMSOHxr8A4m+eweiXim9AAAAAElFTkSuQmCC";
    public static List<String> genderValues = Arrays.asList("Male", "Female", "Others");
    public static List<String> ethinicityValues = Arrays.asList("White (Not of Hispanic Origin)", "African American/Black (Not of Hispanic Origin)",
            "Asian", "Native Hawaiian or other pacific Islanders", "Hispanic", "American Indian/Alaskan Native", "Two or more races", "Decline to Identify");
    public static List<String> salaryRateValues = Arrays.asList("Bi-weekly", "Fee Basis", "Per Year", "Per Month", "Per Day", "Per Hour", "Piece Work", "Student Stipend Paid", "School Year", "Without Compensation");
    public static List<String> workModeValues = Arrays.asList("Onsite-Hybrid", "Onsite-In Office", "Remote");
    public static List<String> jobTypeValues = Arrays.asList("Full Time", "Part Time", "Contract", "Internship", "Freelance");
    public static List<String> noticePeriodValues = Arrays.asList("Immediate", "Less Than 15 days");
    public static List<String> citizenshipValues = Arrays.asList("U.S. Citizen", "Non U.S. Citizen", "U.S. National", "H-1 Visa", "Green Card", "Lawful Permanent Resident Alien");
    public static List<String> relocationValues = Arrays.asList("Within North East", "Within Mid Atlantic", "Within South", "Within West", "Within Mid West", "Within Entire U.S.", "Within Country", "Any location");

}

