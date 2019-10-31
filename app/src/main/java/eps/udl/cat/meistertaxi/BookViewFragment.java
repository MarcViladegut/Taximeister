package eps.udl.cat.meistertaxi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;


public class DriverViewFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    public BookViewFragment() {
        // Required empty public constructor
    }

    public void setParent(Pages parent){
        if(this.parent != null)
            parents.add(this.parent);
        this.parent = parent;

    }

    public void setBook(Book book){
        if(this.book != null)
            prevoiusBooks.add(this.book);
        this.book = book;
        setContent();
    }


    public static BookViewFragment newInstance(Book book) {
        BookViewFragment fragment = new BookViewFragment();
        fragment.book = book;
        return fragment;
    }

    private void showReviewsPopup(){
        ReviewsPopup dialog =  new ReviewsPopup();
        //dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //dialog = new ProfileDialog(post.getGuide(), this, post.getPlace());
        FragmentTransaction ft2 = getFragmentManager().beginTransaction();
        dialog.show(ft2, "reviews_fragment");
        //dialog.setW
        //dialog.setWindow(RelativeLayout.LayoutParams.MATCH_PARENT);
        //dialog.getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        User user = MockupsValues.getUser();
        prevoiusBooks = new ArrayList<>();
        parents = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_book_view, container, false);
        ImageView backButton = (ImageView) view.findViewById(R.id.go_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGoBackButtonClicked();
            }
        });
        if(book != null){
            setContent();
        }

        return view;
    }

    /*@Override
    public void onItemClick(View view, int position) {
        //showBookFragment();

    }*/

    private void showBookFragment(Book book){
        MainActivity activity = (MainActivity) getActivity();
        activity.goToBookPage(book, Pages.BOOK_VIEW_PAGE);
    }

    private void setContent(){
        //if(book != null)
        //    prevoiusBooks.add()
    }

    private void onGoBackButtonClicked(){
        if(!prevoiusBooks.isEmpty()) {
            book = prevoiusBooks.get(prevoiusBooks.size() - 1);
            parent = parents.get(parents.size() - 1);
            setContent();
            prevoiusBooks.remove(prevoiusBooks.size() - 1);
            parents.remove(parents.size() - 1);
        } else {
            this.book = null;
            goToParentPage();
            //activity.backToDiscoverFragment();
        }
    }

    public void goToParentPage(){
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void onAttach(Context context) {
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void onDetach() {
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
