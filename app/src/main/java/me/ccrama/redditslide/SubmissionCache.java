package me.ccrama.redditslide;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;

import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;

import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by carlo_000 on 4/22/2016.
 */
public class SubmissionCache {
    private static WeakHashMap<String, SpannableStringBuilder> titles;
    private static WeakHashMap<String, SpannableStringBuilder> info;

    public static void cacheSubmissions(List<Submission> submissions, Context mContext,
            String baseSub) {
        cacheInfo(submissions, mContext, baseSub);
    }

    private static void cacheInfo(List<Submission> submissions, Context mContext, String baseSub) {
        if (titles == null) titles = new WeakHashMap<>();
        if (info == null) info = new WeakHashMap<>();
        for (Submission submission : submissions) {
            titles.put(submission.getFullName(), getTitleSpannable(submission, mContext));
            info.put(submission.getFullName(), getInfoSpannable(submission, mContext, baseSub));
        }
    }
    
    public static void updateInfoSpannable(Submission changed, Context mContext, String baseSub){
        info.put(changed.getFullName(), getInfoSpannable(changed,mContext, baseSub));
    }

    public static void updateTitleFlair(Submission s, String flair, Context c) {
        titles.put(s.getFullName(), getTitleSpannable(s, flair, c));

    }

    public static SpannableStringBuilder getTitleLine(Submission s, Context mContext) {
        if (titles == null) titles = new WeakHashMap<>();
        if (titles.containsKey(s.getFullName())) {
            return titles.get(s.getFullName());
        } else {
            return getTitleSpannable(s, mContext);
        }
    }

    public static SpannableStringBuilder getInfoLine(Submission s, Context mContext,
            String baseSub) {
        if (info == null) info = new WeakHashMap<>();
        if (info.containsKey(s.getFullName())) {
            return info.get(s.getFullName());
        } else {
            return getInfoSpannable(s, mContext, baseSub);
        }
    }

    private static SpannableStringBuilder getInfoSpannable(Submission submission, Context mContext,
            String baseSub) {
        String spacer = mContext.getString(R.string.submission_properties_seperator);
        SpannableStringBuilder titleString = new SpannableStringBuilder();

        SpannableStringBuilder subreddit =
                new SpannableStringBuilder(" /r/" + submission.getSubredditName() + " ");
        if(submission.getSubredditName() == null){
            subreddit = new SpannableStringBuilder("Promoted ");
        }
        String subname;
        if (submission.getSubredditName() != null) {
            subname = submission.getSubredditName().toLowerCase();
        } else {
            subname = "";
        }
        if (baseSub == null || baseSub.isEmpty()) baseSub = subname;
        if ((SettingValues.colorSubName && Palette.getColor(subname) != Palette.getDefaultColor())
                || (baseSub.equals("nomatching") && (SettingValues.colorSubName
                && Palette.getColor(subname) != Palette.getDefaultColor()))) {
            boolean secondary = (baseSub.equalsIgnoreCase("frontpage")
                    || (baseSub.equalsIgnoreCase("all"))
                    || (baseSub.equalsIgnoreCase("popular"))
                    || (baseSub.equalsIgnoreCase("friends"))
                    || (baseSub.equalsIgnoreCase("mod"))
                    || baseSub.contains(".")
                    || baseSub.contains("+"));
            if (!secondary && !SettingValues.colorEverywhere || secondary) {
                subreddit.setSpan(new ForegroundColorSpan(Palette.getColor(subname)), 0,
                        subreddit.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                subreddit.setSpan(new StyleSpan(Typeface.BOLD), 0, subreddit.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        titleString.append(subreddit);
        titleString.append(spacer);

        try {
            String time = TimeUtils.getTimeAgo(submission.getCreated().getTime(), mContext);
            titleString.append(time);
        } catch (Exception e) {
            titleString.append("just now");
        }
        titleString.append(((submission.getEdited() != null) ? " (edit " + TimeUtils.getTimeAgo(
                submission.getEdited().getTime(), mContext) + ")" : ""));

        titleString.append(spacer);

        SpannableStringBuilder author =
                new SpannableStringBuilder(" " + submission.getAuthor() + " ");
        int authorcolor = Palette.getFontColorUser(submission.getAuthor());

        if (submission.getAuthor() != null) {
            if (Authentication.name != null && submission.getAuthor()
                    .toLowerCase()
                    .equals(Authentication.name.toLowerCase())) {
                author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white,
                                R.color.md_deep_orange_300, false), 0, author.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (submission.getDistinguishedStatus() == DistinguishedStatus.ADMIN) {
                author.setSpan(
                        new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_300,
                                false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (submission.getDistinguishedStatus() == DistinguishedStatus.SPECIAL) {
                author.setSpan(
                        new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_purple_300,
                                false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (submission.getDistinguishedStatus() == DistinguishedStatus.MODERATOR) {
                author.setSpan(
                        new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300,
                                false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (authorcolor != 0) {
                author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            titleString.append(author);
        }


      /*todo maybe?  titleString.append(((comment.hasBeenEdited() && comment.getEditDate() != null) ? " *" + TimeUtils.getTimeAgo(comment.getEditDate().getTime(), mContext) : ""));
        titleString.append("  ");*/

        TypedArray a = mContext.obtainStyledAttributes(new FontPreferences(mContext).getPostFontStyle().getResId(), R.styleable.FontStyle);
        int fontsize = a.getDimensionPixelSize(R.styleable.FontStyle_font_cardinfo, -1);
        a.recycle();

        //MMM Icons
        titleString.append(" ");
        titleString = MMMData.addIcons(mContext, titleString, fontsize, submission.getAuthor(), submission.getSubredditName());


        if (UserTags.isUserTagged(submission.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(
                    " " + UserTags.getUserTag(submission.getAuthor()) + " ");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_500, false),
                    0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }

        if (UserSubscriptions.friends.contains(submission.getAuthor())) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(
                    " " + mContext.getString(R.string.profile_friend) + " ");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_500,
                            false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }


        /* too big, might add later todo
        if (submission.getAuthorFlair() != null && submission.getAuthorFlair().getText() != null && !submission.getAuthorFlair().getText().isEmpty()) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + submission.getAuthorFlair().getText() + " ");
            pinned.setSpan(new RoundedBackgroundSpan(holder.title.getCurrentTextColor(), color, false, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }



        if (holder.leadImage.getVisibility() == View.GONE && !full) {
            String text = "";

            switch (ContentType.getContentType(submission)) {
                case NSFW_IMAGE:
                    text = mContext.getString(R.string.type_nsfw_img);
                    break;

                case NSFW_GIF:
                case NSFW_GFY:
                    text = mContext.getString(R.string.type_nsfw_gif);
                    break;

                case REDDIT:
                    text = mContext.getString(R.string.type_reddit);
                    break;

                case LINK:
                case IMAGE_LINK:
                    text = mContext.getString(R.string.type_link);
                    break;

                case NSFW_LINK:
                    text = mContext.getString(R.string.type_nsfw_link);

                    break;
                case STREAMABLE:
                    text = ("Streamable");
                    break;
                case SELF:
                    text = ("Selftext");
                    break;

                case ALBUM:
                    text = mContext.getString(R.string.type_album);
                    break;

                case IMAGE:
                    text = mContext.getString(R.string.type_img);
                    break;
                case IMGUR:
                    text = mContext.getString(R.string.type_imgur);
                    break;
                case GFY:
                case GIF:
                case NONE_GFY:
                case NONE_GIF:
                    text = mContext.getString(R.string.type_gif);
                    break;

                case NONE:
                    text = mContext.getString(R.string.type_title_only);
                    break;

                case NONE_IMAGE:
                    text = mContext.getString(R.string.type_img);
                    break;

                case VIDEO:
                    text = mContext.getString(R.string.type_vid);
                    break;

                case EMBEDDED:
                    text = mContext.getString(R.string.type_emb);
                    break;

                case NONE_URL:
                    text = mContext.getString(R.string.type_link);
                    break;
            }
            if(!text.isEmpty()) {
                titleString.append(" \n");
                text = text.toUpperCase();
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = mContext.getTheme();
                theme.resolveAttribute(R.attr.activity_background, typedValue, true);
                int color = typedValue.data;
                SpannableStringBuilder pinned = new SpannableStringBuilder(" " + text + " ");
                pinned.setSpan(new RoundedBackgroundSpan(holder.title.getCurrentTextColor(), color, false, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                titleString.append(pinned);
            }
        }*/
        if (SettingValues.showDomain) {
            titleString.append(spacer);
            titleString.append(submission.getDomain());
        }

        if (SettingValues.typeInfoLine) {
            titleString.append(spacer);
            SpannableStringBuilder s = new SpannableStringBuilder(
                    ContentType.getContentDescription(submission, mContext));
            s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(s);
        }
        if (SettingValues.votesInfoLine) {
            titleString.append("\n ");
            SpannableStringBuilder s = new SpannableStringBuilder(
                    submission.getScore()
                            + String.format(Locale.getDefault(), " %s", mContext.getResources()
                            .getQuantityString(R.plurals.points, submission.getScore()))
                            + spacer
                            + submission.getCommentCount()
                            + String.format(Locale.getDefault(), " %s", mContext.getResources()
                            .getQuantityString(R.plurals.comments, submission.getCommentCount())));
            s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (SettingValues.commentLastVisit) {
                final int more = LastComments.commentsSince(submission);
                s.append(more > 0 ? "(+" + more + ")" : "");

            }
            titleString.append(s);
        }
        if (removed.contains(submission.getFullName()) || (submission.getBannedBy() != null
                && !approved.contains(submission.getFullName()))) {
            titleString.append(createRemovedLine(
                    (submission.getBannedBy() == null) ? Authentication.name : submission.getBannedBy(),
                    mContext));
        } else if (approved.contains(submission.getFullName()) || (submission.getApprovedBy()
                != null && !removed.contains(submission.getFullName()))) {
            titleString.append(createApprovedLine(
                    (submission.getApprovedBy() == null) ? Authentication.name
                            : submission.getApprovedBy(), mContext));
        }
        return titleString;
    }

    private static SpannableStringBuilder getTitleSpannable(Submission submission,
            String flairOverride, Context mContext) {
        SpannableStringBuilder titleString = new SpannableStringBuilder();
        titleString.append(Html.fromHtml(submission.getTitle()));

        if (submission.isStickied()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0"
                    + mContext.getString(R.string.submission_stickied).toUpperCase()
                    + "\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, true),
                    0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        if (submission.getTimesGilded() > 0) {
            //if the post has only been gilded once, don't show a number
            final String timesGilded = (submission.getTimesGilded() == 1) ? "" : "\u200Ax" + Integer.toString(submission.getTimesGilded());
            SpannableStringBuilder gilded = new SpannableStringBuilder("\u00A0★" + timesGilded + "\u00A0");
            TypedArray a = mContext.obtainStyledAttributes(new FontPreferences(mContext).getPostFontStyle().getResId(), R.styleable.FontStyle);
            int fontsize = (int) (a.getDimensionPixelSize(R.styleable.FontStyle_font_cardtitle, -1)*.75);
            a.recycle();
            Bitmap image = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.gold);
            float aspectRatio = (float) (1.00 * image.getWidth() / image.getHeight());
            image = Bitmap.createScaledBitmap(image,
                    (int) Math.ceil(fontsize * aspectRatio),
                    (int) Math.ceil(fontsize), true);
            gilded.setSpan(new ImageSpan(mContext, image, ImageSpan.ALIGN_BASELINE), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            gilded.setSpan(new RelativeSizeSpan(0.75f), 3, gilded.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(gilded);
        }
        if (submission.isNsfw()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0NSFW\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_red_300, true), 0,
                    pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        if (submission.getDataNode().get("spoiler").asBoolean()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder("\u00A0SPOILER\u00A0");
            pinned.setSpan(
                    new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_grey_600, true), 0,
                    pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }

        if (submission.getSubmissionFlair().getText() != null && !submission.getSubmissionFlair()
                .getText()
                .isEmpty() || flairOverride != null || (submission.getSubmissionFlair()
                .getCssClass() != null)) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, false);
            int color = typedValue.data;
            theme.resolveAttribute(R.attr.font, typedValue, false);
            int font = typedValue.data;
            String flairString;
            if (flairOverride != null) {
                flairString = flairOverride;
            } else if ((submission.getSubmissionFlair().getText() == null
                    || submission.getSubmissionFlair().getText().isEmpty())
                    && submission.getSubmissionFlair().getCssClass() != null) {
                flairString = submission.getSubmissionFlair().getCssClass();
            } else {
                flairString = submission.getSubmissionFlair().getText();
            }
            SpannableStringBuilder pinned =
                    new SpannableStringBuilder("\u00A0" + Html.fromHtml(flairString) + "\u00A0");
            pinned.setSpan(new RoundedBackgroundSpan(font, color, true, mContext), 0,
                    pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(" ");
            titleString.append(pinned);
        }
        return titleString;

    }

    public static SpannableStringBuilder createRemovedLine(String removedBy, Context c) {
        SpannableStringBuilder removedString = new SpannableStringBuilder("\n");
        SpannableStringBuilder mod = new SpannableStringBuilder("Removed by ");
        mod.append(removedBy);
        mod.setSpan(new StyleSpan(Typeface.BOLD), 0, mod.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod.setSpan(new RelativeSizeSpan(0.8f), 0, mod.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod.setSpan(new ForegroundColorSpan(c.getResources().getColor(R.color.md_red_300)), 0,
                mod.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        removedString.append(mod);
        return removedString;
    }

    public static SpannableStringBuilder createApprovedLine(String removedBy, Context c) {
        SpannableStringBuilder removedString = new SpannableStringBuilder("\n");
        SpannableStringBuilder mod = new SpannableStringBuilder("Approved by ");
        mod.append(removedBy);
        mod.setSpan(new StyleSpan(Typeface.BOLD), 0, mod.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod.setSpan(new RelativeSizeSpan(0.8f), 0, mod.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mod.setSpan(new ForegroundColorSpan(c.getResources().getColor(R.color.md_green_300)), 0,
                mod.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        removedString.append(mod);
        return removedString;
    }

    public static ArrayList<String> removed  = new ArrayList<>();
    public static ArrayList<String> approved = new ArrayList<>();

    private static SpannableStringBuilder getTitleSpannable(Submission submission,
            Context mContext) {
        return getTitleSpannable(submission, null, mContext);
    }

    public static void evictAll() {
        info = new WeakHashMap<>();
    }
}
