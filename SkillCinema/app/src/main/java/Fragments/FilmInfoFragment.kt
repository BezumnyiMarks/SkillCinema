package Fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ItemSnapshotList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.ConcatAdapter
import com.bumptech.glide.Glide
import com.example.skillcinema.App
import Adapters.BottomSheetAdapterAdd
import Adapters.BottomSheetAdapterHeader
import Adapters.BottomSheetAdapterItem
import DataModels.BottomSheetItemDataModel
import DataModels.DBCountry
import DataModels.DBGenre
import DataModels.DBImage
import com.example.skillcinema.DBViewModel
import DataModels.DetailedInfo
import Adapters.GalleryPagingAdapter
import PagingSources.GalleryPagingSource
import com.example.skillcinema.HomeViewModel
import DataModels.Item
import Adapters.ListAdapter
import DataModels.MaxFilmInfo
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import DataModels.Staff
import Adapters.StaffAdapter
import com.example.skillcinema.databinding.BottomSheetBinding
import com.example.skillcinema.databinding.CentralSheetBinding
import com.example.skillcinema.databinding.DeleteCollectionDialogBinding
import com.example.skillcinema.databinding.FragmentFilmInfoBinding
import com.example.skillcinema.utils.Extensions
import kotlinx.coroutines.delay

private const val STAFF_ID = "staffID"
private const val KINOPOISK_ID = "kinopoiskID"
private const val FAVOURITES = "Любимое"
private const val BOOKMARK = "Хочу посмотреть"
private const val WATCHED = "Просмотрено"
private const val INTERESTING = "Было интересно"
private const val SERIES_NAME = "SERIES_NAME"
private const val CURRENT_POSITION = "CURRENT_POSITION"
private const val CURRENT_PAGE = "CURRENT_PAGE"
private const val CURRENT_TYPE_INDEX = "CURRENT_TYPE_INDEX"
private const val TYPE = "TYPE"
class FilmInfoFragment : Fragment() {

    private var _binding: FragmentFilmInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val dBViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val filmInfoDao = (requireActivity().application as App).db.filmInfoDao()
                return DBViewModel(filmInfoDao) as T
            }
        }
    }
    private val actorsAdapter = StaffAdapter { staff -> onStaffClick(staff) }
    private val othersAdapter = StaffAdapter { staff -> onStaffClick(staff) }
    private val similarsAdapter = ListAdapter { item -> onItemClick(item) }
    private val galleryAdapter = GalleryPagingAdapter { item -> onImageClick(item) }
    private val rep = Repository()
    private val extensions = Extensions()
    private var galleryItems: ItemSnapshotList<Item>
    private var pagingSourceFactory = GalleryPagingSource("",0, "", 0, null, listOf())
    private var nameStr = ""
    private var seriesName = ""
    private var description = ""
    private var cutDescription = ""
    private var apiKey = ""

    private var _bSDialog: Dialog? = null
    private val bSDialog get() = _bSDialog!!

    private var kinopoiskID: Long? = null
    init {
        galleryItems = galleryAdapter.snapshot()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kinopoiskID = it.getLong(KINOPOISK_ID)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFilmInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiKey = rep.getApiKey(requireActivity())!!
        showDetailedInfo()
        showCollectionIcon(FAVOURITES)
        showCollectionIcon(BOOKMARK)
        showCollectionIcon(WATCHED)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.favouriteIcon.setOnClickListener {
            setCollection(FAVOURITES)
        }

        binding.bookmarkIcon.setOnClickListener {
            setCollection(BOOKMARK)
        }

        binding.watchedIcon.setOnClickListener {
            setCollection(WATCHED)
        }

        binding.collectionMenuIcon.setOnClickListener {
            showDialog()
        }

        binding.shareIcon.setOnClickListener {
            val intent = Intent()
            intent.setAction(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, "https://www.kinopoisk.ru/film/$kinopoiskID/")
            intent.setType("text/plain")
            val share = Intent.createChooser(intent, null)
            startActivity(share)
        }

        binding.buttonAllGallery.setOnClickListener{
            val bundle = bundleOf(KINOPOISK_ID to kinopoiskID)
            findNavController().navigate(R.id.action_filmInfoFragment_to_galleryFragment, bundle)
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonAllSeasons.setOnClickListener {
            val bundle = bundleOf(KINOPOISK_ID to kinopoiskID, SERIES_NAME to seriesName)
            findNavController().navigate(R.id.action_filmInfoFragment_to_seasonsFragment, bundle)
        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> findNavController().navigate(R.id.homeFragment)
                R.id.search -> findNavController().navigate(R.id.searchFragment)
                R.id.profile -> findNavController().navigate(R.id.profileFragment)
            }
            true
        }

        binding.description.setAnimationDuration(400)
        binding.description.setOnClickListener {
            if (binding.description.text.length >= 250){
                if (binding.description.isExpanded){
                    binding.description.collapse()
                    lifecycleScope.launchWhenCreated {
                        binding.description.isClickable = false
                        delay(380)
                        binding.description.text = cutDescription
                        binding.description.isClickable = true
                    }
                }
                else{
                    binding.description.text = description
                    binding.description.expand()
                    lifecycleScope.launchWhenCreated {
                        binding.description.isClickable = false
                        delay(410)
                        binding.description.isClickable = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onStaffClick(staff: Staff) {
        val deleteDialog = Dialog(requireActivity())
        val deleteBinding = DeleteCollectionDialogBinding.inflate(layoutInflater)
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog.setContentView(deleteBinding.root)
        deleteDialog.show()
        deleteDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        deleteDialog.window?.setGravity(Gravity.CENTER)
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //deleteBinding.textView.textSize = 16F
        deleteBinding.textView.text = "Внимание! данная функция предполагает длительную загрузку, а также быстрое исчерпание лимита запросов. Изволите продолжить?"

        deleteBinding.buttonPositive.setOnClickListener {
            Toast.makeText(requireContext(), "Ну что ж, Вы сами избрали свою судьбу... Теперь извольте ожидать:)", Toast.LENGTH_LONG).show()
            deleteDialog.hide()
            val bundle = bundleOf(STAFF_ID to staff.staffID)
            findNavController().navigate(R.id.action_filmInfoFragment_to_staffInfoFragment, bundle)
        }

        deleteBinding.buttonNegative.setOnClickListener {
            Toast.makeText(requireContext(), "Вот и правильно. Нечего запросы по чём зря транжирить:)", Toast.LENGTH_LONG).show()
            deleteDialog.hide()
        }
    }

    private fun onItemClick(item: Item) {
        val bundle = bundleOf(KINOPOISK_ID to item.filmID)
        findNavController().navigate(R.id.action_filmInfoFragment_self, bundle)
    }

    private fun onImageClick(selectedItem: Item) {
        var currentPosition = 0
        galleryItems.items.forEachIndexed { index, item ->
            if (item == selectedItem)
                currentPosition = index
        }
        val bundle = bundleOf(
            CURRENT_POSITION to currentPosition,
            CURRENT_PAGE to pagingSourceFactory.currentPage,
            CURRENT_TYPE_INDEX to pagingSourceFactory.currentTypeIndex,
            TYPE to "All",
            KINOPOISK_ID to kinopoiskID!!.toInt()
        )
        findNavController().navigate(R.id.action_filmInfoFragment_to_pictureFragment, bundle)
    }

    private fun onBSCheckboxClick(item: BottomSheetItemDataModel){
        val collection = item
        val filmsIDList = extensions.convertStringToList(rep.getCollection(requireContext(), collection.collectionName)).toMutableList()

        collection.includesCurrentFilm = !collection.includesCurrentFilm
        if(filmsIDList.contains(kinopoiskID.toString()) && !collection.includesCurrentFilm){
            filmsIDList.remove(kinopoiskID.toString())
        }
        if (!filmsIDList.contains(kinopoiskID.toString()) && collection.includesCurrentFilm){
            filmsIDList.add(kinopoiskID.toString())
        }
        Log.d("filmsIDList", filmsIDList.toString())
        rep.saveCollection(requireActivity(), extensions.convertListToString(filmsIDList), collection.collectionName)

        when(collection.collectionName){
            BOOKMARK -> showCollectionIcon(BOOKMARK)
            FAVOURITES -> showCollectionIcon(FAVOURITES)
        }
    }

    private fun onBSAddClick(){
        showAddDialog()
    }

    private fun showDetailedInfo(){
        binding.actorsRecyclerView.adapter = actorsAdapter
        binding.othersRecyclerView.adapter = othersAdapter
        binding.similarFilmsRecyclerView.adapter = similarsAdapter
        binding.galleryRecyclerView.adapter = galleryAdapter

        var loadFromDB = false
        val maxFilmInfoList = mutableListOf<MaxFilmInfo>()
        val maxFilmInfoIDList = mutableListOf<Long>()
        val collectionWatchedIDList = extensions.convertStringToList(rep.getCollection(requireContext(), WATCHED))

        viewModel.loadSimilars(apiKey, kinopoiskID!!.toInt())
        viewModel.loadStaff(apiKey, kinopoiskID!!.toInt())
        lifecycleScope.launchWhenCreated {

            dBViewModel.getMaxFilmInfoList().forEach {
                maxFilmInfoList.add(it)
                maxFilmInfoIDList.add(it.filmInfo.filmID)
            }
            if (maxFilmInfoIDList.contains(kinopoiskID)){
                loadFromDB = true
                val dBFilmInfo = dBViewModel.getMaxFilmInfoByID(kinopoiskID!!)
                val countries = extensions.getCountriesList(dBFilmInfo.country)
                val genres = extensions.getGenresList(dBFilmInfo.genre)
                Log.d("filmInfo", dBFilmInfo.toString())
                viewModel._detailedInfo.value = DetailedInfo(dBFilmInfo.filmInfo.filmID,"", dBFilmInfo.filmInfo.nameRu, dBFilmInfo.filmInfo.nameEn, dBFilmInfo.filmInfo.nameOriginal, dBFilmInfo.filmInfo.posterURL,"","", dBFilmInfo.filmInfo.logoURL,0L,0.0,0L, dBFilmInfo.filmInfo.ratingKinopoisk,0L, dBFilmInfo.filmInfo.ratingImdb,0L, dBFilmInfo.filmInfo.ratingFilmCritics,0L,0.0,0L,0.0,0L,"", dBFilmInfo.filmInfo.year, dBFilmInfo.filmInfo.filmLength,"", dBFilmInfo.filmInfo.description, dBFilmInfo.filmInfo.shortDescription,"",true,"","","", dBFilmInfo.filmInfo.ratingAgeLimits,true,true,"",  countries,  genres,0L,0L, dBFilmInfo.filmInfo.serial,true,true)
            }
            else {
                viewModel.loadDetailedInfo(apiKey, kinopoiskID!!.toInt())
            }
            viewModel.detailedInfoLoading.collect{ detailedInfoLoading ->
                if (!detailedInfoLoading){
                    viewModel.detailedInfo.collect{ detailedInfo->
                        if (detailedInfo.serial == true){
                            seriesName = detailedInfo.nameRu ?: detailedInfo.nameEn ?: detailedInfo.nameOriginal ?: "Безымянный сериал"
                            binding.seasonsField.visibility = View.VISIBLE
                            lifecycleScope.launchWhenCreated {
                                viewModel.loadSeasons(apiKey, kinopoiskID!!.toInt())
                                viewModel.seasonsLoading.collect{ seasonsLoading ->
                                    if (!seasonsLoading){
                                        viewModel.seasons.collect{
                                            var seasonsText = ""
                                            if (it.total != null)
                                                seasonsText = resources.getQuantityString(R.plurals.seasons, it.total.toInt(), it.total.toInt())

                                            var seriesNumber = 0
                                            if (it.items.isNotEmpty()){
                                                it.items.forEach { season ->
                                                    seriesNumber += season.episodes.size
                                                }
                                            }
                                            seasonsText = "$seasonsText, ${resources.getQuantityString(
                                                R.plurals.series, seriesNumber, seriesNumber)}"

                                            binding.seasonsNumber.text = seasonsText
                                        }
                                    }
                                }
                            }
                        }

                        if (!loadFromDB && detailedInfo.kinopoiskID != null){
                            val dBCountryIDList = mutableListOf<Int>()
                            val dBGenreIDList = mutableListOf<Int>()
                            var countries: List<DBCountry> = listOf()
                            var genres: List<DBGenre> = listOf()

                            maxFilmInfoList.forEach { maxFilmInfo ->
                                maxFilmInfo.country.forEach { dBCountryIDList.add(it.id) }
                                maxFilmInfo.genre.forEach { dBGenreIDList.add(it.id) }
                            }

                            if (dBCountryIDList.isEmpty() || dBGenreIDList.isEmpty()){
                                countries = extensions.getDBCountriesList(detailedInfo.countries!!, kinopoiskID!!, 0)
                                genres = extensions.getDBGenresList(detailedInfo.genres!!, kinopoiskID!!,0)
                            }
                            else{
                                countries = extensions.getDBCountriesList(detailedInfo.countries!!, kinopoiskID!!, dBCountryIDList.max())
                                genres = extensions.getDBGenresList(detailedInfo.genres!!, kinopoiskID!!, dBViewModel.getDBGenresMaxID())
                            }

                            maxFilmInfoIDList.clear()
                            dBViewModel.getMaxFilmInfoList().forEach {
                                maxFilmInfoIDList.add(it.filmInfo.filmID)
                            }
                            if (!maxFilmInfoIDList.contains(kinopoiskID))
                                dBViewModel.addMaxFilmInfo(
                                    extensions.getFilmInfo(detailedInfo),
                                    countries, genres)
                        }

                        val interestingFilmsIDList = extensions.convertStringToList(rep.getCollection(requireActivity(), INTERESTING)).toMutableList()
                        if(!interestingFilmsIDList.contains(kinopoiskID.toString())){
                            interestingFilmsIDList.add(kinopoiskID.toString())
                            rep.saveCollection(requireActivity(), extensions.convertListToString(interestingFilmsIDList), INTERESTING)
                        }

                        if (detailedInfo.posterURL != null)
                            Glide
                                .with(this@FilmInfoFragment)
                                .load(android.net.Uri.parse(detailedInfo.posterURL))
                                .centerCrop()
                                .into(binding.posterImageView)

                        if (detailedInfo.logoURL != null)
                            Glide
                                .with(this@FilmInfoFragment)
                                .load(android.net.Uri.parse(detailedInfo.logoURL))
                                .centerCrop()
                                .into(binding.logoImageView)

                        if (detailedInfo.ratingKinopoisk != null || detailedInfo.ratingImdb != null || detailedInfo.ratingFilmCritics != null)
                            nameStr = "${detailedInfo.ratingKinopoisk ?: detailedInfo.ratingImdb ?: detailedInfo.ratingFilmCritics}, "
                        nameStr += if (detailedInfo.logoURL != null && detailedInfo.nameOriginal != null)
                            "${detailedInfo.nameOriginal}"
                        else "${detailedInfo.nameRu ?: detailedInfo.nameOriginal ?: detailedInfo.nameEn}"
                        binding.name.text = nameStr

                        val genreText = "${detailedInfo.year.toString()} ${detailedInfo.genres.let {extensions.getGenreSet(it!!)}}"
                        binding.genre.text = genreText

                        var filmHours = 0
                        var filmMinutes = 0
                        var countryStr = ""
                        if (detailedInfo.filmLength != null){
                            if (detailedInfo.filmLength >= 60){
                                filmHours = detailedInfo.filmLength.toInt()/60
                                filmMinutes = detailedInfo.filmLength.toInt()%60
                            }
                            else filmMinutes = detailedInfo.filmLength.toInt()

                            if (detailedInfo.countries != null && detailedInfo.countries.isNotEmpty())
                                countryStr = "${detailedInfo.countries.get(0).country}, $filmHours ч $filmMinutes мин"
                            else  countryStr = "$filmHours ч $filmMinutes мин"
                        }
                        else if (detailedInfo.countries != null && detailedInfo.countries.isNotEmpty())
                            countryStr = detailedInfo.countries.get(0).country

                        if (detailedInfo.ratingAgeLimits != null)
                            countryStr += ", ${detailedInfo.ratingAgeLimits.replace("age", "")}+"
                        binding.country.text = countryStr

                        if (detailedInfo.shortDescription != null)
                            binding.shortDescription.text = detailedInfo.shortDescription
                        else binding.shortDescription.setVisibility(View.GONE)

                        if (detailedInfo.description != null){
                            binding.description.text = detailedInfo.description
                            description = detailedInfo.description
                        }
                        else binding.description.setVisibility(View.GONE)
                        if (description.length >= 250){
                            cutDescription = ""
                            for (i in 0..249){
                                cutDescription += description[i]
                            }
                            repeat(3){
                                cutDescription += "."
                            }
                            binding.description.text = cutDescription
                            binding.description.maxLines = binding.description.lineCount
                            binding.description.collapse()
                        }
                        else{
                            binding.description.text = description
                            binding.description.maxLines = binding.description.lineCount
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.staffListLoading.collect { staffListLoading ->
                if (!staffListLoading) {
                    viewModel.staffList.collect{ staffList ->
                        val actorsList = mutableListOf<Staff>()
                        val othersList = mutableListOf<Staff>()

                        staffList.forEach {
                            when(it.professionKey){
                                "ACTOR" -> actorsList.add(it)
                                else -> othersList.add(it)
                            }
                        }
                        if (actorsList.isNotEmpty())
                            actorsAdapter.submitList(actorsList)
                        else{
                            binding.actorsListHeader.visibility = View.GONE
                            binding.actorsRecyclerView.visibility = View.GONE
                        }
                        if (othersList.isNotEmpty())
                            othersAdapter.submitList(othersList)
                        else{
                            binding.othersListHeader.visibility = View.GONE
                            binding.othersRecyclerView.visibility = View.GONE
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.similarsLoading.collect { similarsLoading ->
                if (!similarsLoading) {
                    viewModel.similars.collect{ similars ->
                        if (similars.items.isNotEmpty()) {
                            similars.items.forEach {
                                it.watched = collectionWatchedIDList.contains(it.filmID.toString())
                            }
                            similarsAdapter.submitList(similars.items)
                        }
                        else{
                            binding.similarFilmsHeader.visibility = View.GONE
                            binding.similarFilmsRecyclerView.visibility = View.GONE
                        }
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            pagingSourceFactory = GalleryPagingSource(apiKey, kinopoiskID!!.toInt(), "All", 1, viewModel, listOf())
            pagingSourceFactory.netLoadingAllowed = true
            viewModel.gallery = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { pagingSourceFactory }
            ).flow.cachedIn(lifecycleScope)

            lifecycleScope.launchWhenCreated {
                viewModel.gallery.collect {
                    galleryAdapter.submitData(it)
                }
            }
        }

        galleryAdapter.addOnPagesUpdatedListener {
            galleryItems = galleryAdapter.snapshot()
            galleryItems.forEach {
                dBViewModel.addImage(DBImage(it!!.imageURL!!, "All", kinopoiskID!!.toInt()))
            }

            if (galleryItems.isEmpty()){
                binding.galleryHeader.visibility = View.GONE
                binding.galleryRecyclerView.visibility = View.GONE
            }
            else{
                binding.galleryHeader.visibility = View.VISIBLE
                binding.galleryRecyclerView.visibility = View.VISIBLE
            }

            if (galleryItems.size >= 20){
                binding.buttonAllGallery.visibility = View.VISIBLE
                binding.buttonAllGallery.text = galleryItems.size.toString()
            }
        }
    }

    private fun setCollection(collectionKey: String){
        var collectionIDList = mutableListOf<String>()
        var collectionIDStr = rep.getCollection(requireActivity(), collectionKey)

        if (collectionIDStr == ""){
            collectionIDStr += "$kinopoiskID"
            rep.saveCollection(requireActivity(), collectionIDStr, collectionKey)
            when(collectionKey){
                FAVOURITES -> binding.favouriteIcon.setImageResource(R.drawable.ic_favourite_icon)
                BOOKMARK -> binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_icon)
                WATCHED -> binding.watchedIcon.setImageResource(R.drawable.ic_watched_icon)
            }
        }
        else{
            collectionIDList = collectionIDStr?.split(",") as MutableList<String>
            if (!collectionIDList.contains(kinopoiskID.toString())){
                collectionIDStr += ",$kinopoiskID"
                rep.saveCollection(requireActivity(), collectionIDStr, collectionKey)
                when(collectionKey){
                    FAVOURITES -> binding.favouriteIcon.setImageResource(R.drawable.ic_favourite_icon)
                    BOOKMARK -> binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_icon)
                    WATCHED -> binding.watchedIcon.setImageResource(R.drawable.ic_watched_icon)
                }
            }
            else{
                when(collectionKey){
                    FAVOURITES -> binding.favouriteIcon.setImageResource(R.drawable.ic_favourite_border_icon)
                    BOOKMARK -> binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border_icon)
                    WATCHED -> binding.watchedIcon.setImageResource(R.drawable.ic_not_watched_icon)
                }
                collectionIDStr = ""
                if (collectionIDList.size > 1){
                    collectionIDList.remove(kinopoiskID.toString())
                    collectionIDList.forEach { collectionIDStr += "$it," }
                    collectionIDStr = collectionIDStr.replace(".$".toRegex(), "")
                }
                rep.saveCollection(requireActivity(), collectionIDStr, collectionKey)
            }
        }
    }

    private fun showCollectionIcon(collectionKey: String){
        val collectionIDStr = rep.getCollection(requireActivity(), collectionKey)
        val collectionIDList = collectionIDStr?.split(",")
        if (collectionIDList!!.contains(kinopoiskID.toString())){
            when(collectionKey){
                FAVOURITES -> binding.favouriteIcon.setImageResource(R.drawable.ic_favourite_icon)
                BOOKMARK -> binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_icon)
                WATCHED -> binding.watchedIcon.setImageResource(R.drawable.ic_watched_icon)
            }
        }
        else{
            when(collectionKey){
                FAVOURITES -> binding.favouriteIcon.setImageResource(R.drawable.ic_favourite_border_icon)
                BOOKMARK -> binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border_icon)
                WATCHED -> binding.watchedIcon.setImageResource(R.drawable.ic_not_watched_icon)
            }
        }
    }

    private fun showDialog(){
        _bSDialog = Dialog(requireActivity())
        val bSBinding = BottomSheetBinding.inflate(layoutInflater)
        val bSAdapterHeader = BottomSheetAdapterHeader()
        val bSAdapterItem = BottomSheetAdapterItem {item -> onBSCheckboxClick(item)}
        val bSAdapterAdd = BottomSheetAdapterAdd {onBSAddClick()}
        bSBinding.recyclerView.adapter = ConcatAdapter(bSAdapterHeader, bSAdapterItem, bSAdapterAdd)
        val itemsList = mutableListOf<BottomSheetItemDataModel>()
        bSDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        bSDialog.setContentView(bSBinding.root)
        bSDialog.show()
        bSDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        bSDialog.window?.setGravity(Gravity.BOTTOM)
        bSDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val collectionsNamesList = extensions.convertStringToList(rep.getCollectionsNames(requireActivity()))
        collectionsNamesList.forEach {
            val filmsIDList = extensions.convertStringToList(rep.getCollection(requireActivity(), it))
            val collectionIncludesCurrentFilm = filmsIDList.contains(kinopoiskID.toString())
            itemsList.add(
            BottomSheetItemDataModel(collectionIncludesCurrentFilm, it, filmsIDList.size)
            )
        }
        bSAdapterItem.submitList(itemsList)
        lifecycleScope.launchWhenCreated {
            val maxFilmInfo = dBViewModel.getMaxFilmInfoByID(kinopoiskID!!.toLong())
            if (maxFilmInfo.filmInfo.ratingKinopoisk != null || maxFilmInfo.filmInfo.ratingImdb != null || maxFilmInfo.filmInfo.ratingFilmCritics != null){
                bSBinding.rating.visibility = View.VISIBLE
                bSBinding.rating.text = "${maxFilmInfo.filmInfo.ratingKinopoisk ?: maxFilmInfo.filmInfo.ratingImdb ?: maxFilmInfo.filmInfo.ratingFilmCritics}"
            }
            bSBinding.name.text = maxFilmInfo.filmInfo.nameRu ?: maxFilmInfo.filmInfo.nameEn ?: maxFilmInfo.filmInfo.nameOriginal
            var descriptionStr = ""
            val descriptionList = mutableListOf<String>()
            if (maxFilmInfo.filmInfo.year != null)
                descriptionList.add(maxFilmInfo.filmInfo.year.toString())
            if (maxFilmInfo.genre.isNotEmpty())
                descriptionList.add(extensions.getGenreSet(extensions.getGenresList(maxFilmInfo.genre)))
            descriptionList.forEach { descriptionStr += "$it, "}
            repeat(2){
                descriptionStr = descriptionStr.replace(".$".toRegex(), "")
            }
            bSBinding.description.text = descriptionStr
            Glide
                .with(this@FilmInfoFragment)
                .load(android.net.Uri.parse(maxFilmInfo.filmInfo.posterURL))
                .centerCrop()
                .into(bSBinding.imageView)
        }
        bSBinding.buttonDismiss.setOnClickListener {
            bSDialog.hide()
        }
    }

    private fun showAddDialog(){
        val addDialog = Dialog(requireActivity())
        val addBinding = CentralSheetBinding.inflate(layoutInflater)
        addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addDialog.setContentView(addBinding.root)
        addDialog.show()
        addDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addDialog.window?.setGravity(Gravity.CENTER)
        addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addBinding.buttonSave.setOnClickListener {
            bSDialog.hide()
            val collectionsNamesList = extensions.convertStringToList(rep.getCollectionsNames(requireContext())).toMutableList()

            if (!collectionsNamesList.contains(addBinding.editText.text.toString()) && addBinding.editText.text.toString() != ""){
                collectionsNamesList.add(addBinding.editText.text.toString())
                rep.saveCollection(requireActivity(), kinopoiskID.toString(), addBinding.editText.text.toString())
                rep.saveCollectionsNames(requireActivity(), extensions.convertListToString(collectionsNamesList))
            }
            showDialog()
            addDialog.hide()
        }

        addBinding.buttonDismiss.setOnClickListener {
            addDialog.hide()
        }
    }
}
