package com.example.maciejwikira.prgnv2;

/**
 * Created by Maciej on 2017-11-13.
 */
/*
    Klasa zawiera globalne definicje stałych.
 */
public final class Constants {

    public static final String CARDS_OR_RECEIPTS = "CARDS_OR_RECEIPTS";
    public static final String BROADCAST_ACTION = "com.example.maciejwikira.prgnv2.BROADCAST";
    public static final String IMAGE_PATH = "path";
    public static final String IMAGE_URI = "uri";
    public static final String RECEIPT_VAL = "receipt_value";
    public static final String RECEIPT_DATE = "receipt_date";
    public static final String RECEIPT_TEXT = "receipt_text";
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 44;
    public static final String UPDATE = "update";
    public static final String ITEM_DATA = "data";
    public static final String ITEM_ID = "item_id";
    public static final String RECEIPT_PROCESSING = "receipt_processing";
    public static final int RESULT_PROCESSING = 777;

    public static final String[] cardCategoriesProjection = {
            CardContract.Card_Categories._ID,
            CardContract.Card_Categories.CATEGORY_NAME
    };

    public static final String cardCategoriesSelection =
            CardContract.Card_Categories.CATEGORY_NAME + " = ?";

    public static final  String[] cardTableCols = new String[]{
            CardContract.Card._ID,
            CardContract.Card.NAME,
            CardContract.Card.CATEGORY,
            CardContract.Card.EXPIRATION_DATE,
            CardContract.Card.IMAGE_PATH,
            CardContract.Card.FAVORITED,
            CardContract.Card.DESCRIPTION
    };

    public static final  String[] receiptTableCols = new String[]{
            ReceiptContract.Receipt._ID,
            ReceiptContract.Receipt.NAME,
            ReceiptContract.Receipt.CATEGORY,
            ReceiptContract.Receipt.VALUE,
            ReceiptContract.Receipt.DATE,
            ReceiptContract.Receipt.IMAGE_PATH,
            ReceiptContract.Receipt.CONTENT,
            ReceiptContract.Receipt.FAVORITED,
            ReceiptContract.Receipt.DESCRIPTION,
            ReceiptContract.Receipt.WARRANTY
    };

    public static final String[] receiptPhotosTableCols = new String[]{
            ReceiptContract.Receipt_Photos._ID,
            ReceiptContract.Receipt_Photos.PHOTO_PATH,
            ReceiptContract.Receipt_Photos.RECEIPT_ID
    };

    public static final String[] cardPhotosTableCols = new String[]{
            CardContract.Card_Photos._ID,
            CardContract.Card_Photos.PHOTO_PATH,
            CardContract.Card_Photos.CARD_ID
    };

    public static final String[] fromCardTable = new String[]{
            CardContract.Card.NAME,
            CardContract.Card.CATEGORY,
            CardContract.Card.EXPIRATION_DATE,
            CardContract.Card.IMAGE_PATH
    };

    public static final String[] fromReceiptTable = new String[]{
            ReceiptContract.Receipt.NAME,
            ReceiptContract.Receipt.CATEGORY,
            ReceiptContract.Receipt.DATE,
            ReceiptContract.Receipt.VALUE,
            ReceiptContract.Receipt.IMAGE_PATH,
            ReceiptContract.Receipt.CONTENT,
            ReceiptContract.Receipt.FAVORITED,
            ReceiptContract.Receipt.DESCRIPTION,
            ReceiptContract.Receipt.WARRANTY
    };

    public static final int[] toCardTable = new int[]{
            R.id.nameTextView,
            R.id.categoryTextView,
            R.id.dateView,
            R.id.photoView
    };

    public static final int[] toReceiptTable = new int[]{
            R.id.nameTextView,
            R.id.categoryTextView,
            R.id.dateView,
            R.id.photoView
    };

    public static final String[] receiptCategoriesProjection = {
            ReceiptContract.Categories._ID,
            ReceiptContract.Categories.CATEGORY_NAME
    };

    public static final  String  receiptCategoriesSelection =
            ReceiptContract.Categories.CATEGORY_NAME + " = ?";

}
